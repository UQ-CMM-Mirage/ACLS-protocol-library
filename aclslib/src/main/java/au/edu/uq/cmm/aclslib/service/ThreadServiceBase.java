package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

/**
 * A Service class that extends this base class will inherit infrastructure 
 * to run the service in a Thread, and methods to startup and shutdown the
 * thread. The Service class merely needs to implement the {@link Runnable#run()} 
 * method.
 * 
 * @author scrawley
 */
public abstract class ThreadServiceBase implements Service, Runnable {
    private static final Logger LOG = Logger.getLogger(ThreadServiceBase.class);

    private State state = State.INITIAL;
    private Thread thread;
    private final Object lock = new Object();

    public void startup() {
        synchronized (lock) {
            if (thread == null) {
                thread = new Thread(this);
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable ex) {
                        LOG.error("Thread died", ex);
                        synchronized (lock) {
                            thread = null;
                            state = State.FAILED;
                            lock.notifyAll();
                        }
                    }
                });
            }
            state = State.STARTED;
            thread.start();
            lock.notifyAll();
        }
    }

    public void shutdown() {
        Thread t;
        synchronized (lock) {
            if (thread == null) {
                state = State.STOPPED;
                lock.notifyAll();
                return;
            }
            thread.interrupt();
            t = thread;
            state = State.STOPPING;
        }
        try {
            t.join();
            synchronized (lock) {
                state = State.STOPPED;
                thread = null;
                lock.notifyAll();
            }
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public void awaitShutdown() throws InterruptedException {
        synchronized (lock) {
            while (thread != null) {
                lock.wait();
            }
        }
    }

    public State getState() {
        synchronized (lock) {
            return state;
        }
    }
}
