package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

public abstract class MonitoredThreadServiceBase implements Service, Runnable {
    private static final Logger LOG = Logger.getLogger(MonitoredThreadServiceBase.class);
    
    private class Monitor implements Runnable {
        private Throwable lastException;
        
        public void run() {
            while (true) {
                Thread serviceThread = new Thread(MonitoredThreadServiceBase.this);
                serviceThread.setDaemon(true);
                serviceThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable ex) {
                        lastException = ex;
                        LOG.error("Service thread died", ex);
                    }
                });
                lastException = null;
                serviceThread.start();
                try {
                    serviceThread.join();
                    if (!restartDecider.isRestartable(lastException)) {
                        LOG.error("Service thread not restartable - bailing out");
                        break;
                    }
                } catch (InterruptedException ex) {
                    serviceThread.interrupt();
                    try {
                        serviceThread.join();
                    } catch (InterruptedException ex2) {
                        LOG.error("Monitor thread interrupted while waiting " +
                        		"for service thread to finish", ex);
                    }
                    break;
                }
            }
        }
    }

    private Thread monitorThread;
    private RestartDecider restartDecider;
    
    
    protected MonitoredThreadServiceBase() {
        this(new DefaultRestartDecider());
    }
    
    protected MonitoredThreadServiceBase(RestartDecider restartDecider) {
        this.restartDecider = restartDecider;
    }

    public synchronized void startup() {
        if (monitorThread != null) {
            return;
        }
        monitorThread = new Thread(new Monitor());
        monitorThread.setDaemon(true);
        monitorThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.error("Monitor thread died!", ex);
                monitorThread = null;
            }
        });
        monitorThread.start();
        notifyAll();
    }

    public synchronized void shutdown() {
        monitorThread.interrupt();
        try {
            monitorThread.join();
            monitorThread = null;
            notifyAll();
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public synchronized void awaitShutdown() throws InterruptedException {
        while (monitorThread != null) {
            wait();
        }
    }
}
