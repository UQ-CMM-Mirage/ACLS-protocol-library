package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

public abstract class ThreadServiceBase implements Service, Runnable {
    private static final Logger LOG = Logger.getLogger(ThreadServiceBase.class);

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
        notifyAll();
    }

    public synchronized void shutdown() {
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
}
