package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

/**
 * A Service class that extends this base class will inherit infrastructure to
 * run the service in a thread.  Additional infrastructure will automatically 
 * restart the service thread if it dies with an unhandled exception.
 * The Service class merely needs to implement the {@link Runnable#run()} 
 * method.
 * <p>
 * The restart behavior can be customized by the subclass supplying a
 * {@link RestartDecider} instance in the constructor.
 * 
 * @author scrawley
 */
public abstract class MonitoredThreadServiceBase implements Service, Runnable {
    private static final Logger LOG = Logger.getLogger(MonitoredThreadServiceBase.class);
    
    private class Monitor implements Runnable {
        private Throwable lastException;
        private Thread serviceThread;
        
        public void run() {
            while (true) {
                serviceThread = new Thread(MonitoredThreadServiceBase.this);
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
                        synchronized (MonitoredThreadServiceBase.this) {
                            state = State.FAILED;
                        }
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
                    synchronized (MonitoredThreadServiceBase.this) {
                        state = State.SHUT_DOWN;
                    }
                    break;
                }
            }
        }

        private void interruptServiceThread() {
            if (serviceThread != null) {
                serviceThread.interrupt();
            }
        }
    }

    private State state = State.INITIAL;
    private Thread monitorThread;
    private RestartDecider restartDecider;
    private final Object lock = new Object();
    
    /**
     * Instantiate using a default RestartDecider.
     */
    protected MonitoredThreadServiceBase() {
        this(new DefaultRestartDecider());
    }
    
    /**
     * Instantiate using a supplied RestartDecider
     * @param restartDecider
     */
    protected MonitoredThreadServiceBase(RestartDecider restartDecider) {
        this.restartDecider = restartDecider;
    }

    public final void startup() {
        synchronized (lock) {
            if (monitorThread != null && monitorThread.isAlive()) {
                state = State.RUNNING;
                return;
            }
            final Monitor monitor = new Monitor();
            monitorThread = new Thread(monitor);
            monitorThread.setDaemon(true);
            monitorThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable ex) {
                    LOG.error("Monitor thread died!", ex);
                    synchronized (lock) {
                        monitor.interruptServiceThread();
                        state = State.FAILED;
                    }
                }
            });
            state = State.RUNNING;
            monitorThread.start();
        }
    }

    public final void shutdown() {
        Thread m;
        synchronized (lock) {
            if (monitorThread == null) {
                state = State.SHUT_DOWN;
                lock.notifyAll();
                return;
            }
            monitorThread.interrupt();
            m = monitorThread;
        }
        try {
            m.join();
            synchronized (lock) {
                monitorThread = null;
                state = State.SHUT_DOWN;
                lock.notifyAll();
            }
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public final void awaitShutdown() throws InterruptedException {
        synchronized (lock) {
            while (monitorThread != null) {
                lock.wait();
            }
        }
    }

    public final State getState() {
        synchronized (lock) {
            return state;
        }
    }
}
