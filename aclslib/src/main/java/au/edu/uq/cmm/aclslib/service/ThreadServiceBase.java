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

    public synchronized void startup() {
        if (thread != null) {
            state = State.RUNNING;
            return;
        }
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.error("Thread died", ex);
                synchronized (ThreadServiceBase.this) {
                    thread = null;
                    state = State.FAILED;
                }
            }
        });
        state = State.RUNNING;
        thread.start();
    }

    public void shutdown() {
        Thread t;
        synchronized (this) {
            if (thread == null) {
                state = State.SHUT_DOWN;
                notifyAll();
                return;
            }
            thread.interrupt();
            t = thread;
        }
        try {
            t.join();
            synchronized (this) {
                state = State.SHUT_DOWN;
                thread = null;
                notifyAll();
            }
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    public synchronized void awaitShutdown() throws InterruptedException {
        while (thread != null) {
            wait();
        }
    }

    public synchronized State getState() {
        return state;
    }
    
    
}
