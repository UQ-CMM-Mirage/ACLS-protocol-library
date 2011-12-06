package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

public abstract class MonitoredThreadServiceBase implements Service, Runnable {
    private static final Logger LOG = Logger.getLogger(MonitoredThreadServiceBase.class);
    
    private class Monitor implements Runnable {
        public void run() {
            while (true) {
                Thread serviceThread = new Thread(MonitoredThreadServiceBase.this);
                serviceThread.setDaemon(true);
                serviceThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable ex) {
                        LOG.error("Service thread died", ex);
                    }
                });
                serviceThread.start();
                try {
                    serviceThread.join();
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }

    private Thread watchdogThread;

    public synchronized void startup() {
        if (watchdogThread != null) {
            return;
        }
        watchdogThread = new Thread(new Monitor());
        watchdogThread.setDaemon(true);
        watchdogThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.error("Monitor thread died!", ex);
                watchdogThread = null;
            }
        });
        watchdogThread.start();
        notifyAll();
    }

    public synchronized void shutdown() {
        watchdogThread.interrupt();
        try {
            watchdogThread.join();
            watchdogThread = null;
            notifyAll();
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public synchronized void awaitShutdown() throws InterruptedException {
        while (watchdogThread != null) {
            wait();
        }
    }
}
