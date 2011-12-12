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

    private boolean hasStarted;
    private Thread thread;

    public synchronized void startup() {
        if (thread != null) {
            return;
        }
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.error("Thread died", ex);
                thread = null;
            }
        });
        thread.start();
        hasStarted = true;
        notifyAll();
    }

    public synchronized void shutdown() {
        if (thread == null) {
            return;
        }
        thread.interrupt();
        try {
            thread.join();
            thread = null;
            notifyAll();
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
        if (thread == null) {
            return hasStarted ? State.SHUT_DOWN : State.INITIAL;
        } else {
            return thread.isAlive() ? State.RUNNING : State.FAILED;
        }
    }
    
    
}
