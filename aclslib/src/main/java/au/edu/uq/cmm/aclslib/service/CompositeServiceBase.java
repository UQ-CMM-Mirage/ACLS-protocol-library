package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

public abstract class CompositeServiceBase implements Service {
    private static final Logger LOG = Logger.getLogger(CompositeServiceBase.class);
    private boolean running = false;
    private boolean changingState = false;

    public void startup() {
        synchronized (this) {
            if (changingState) {
                throw new IllegalStateException("State change already in progress");
            }
            if (running == true) {
                return;
            }
            changingState = true;
        }
        LOG.info("Starting up");
        doStartup();
        LOG.info("Startup completed");
        synchronized (this) {
            changingState = false;
            running = true;
            this.notifyAll();
        }
    }

    public synchronized void shutdown() {
        synchronized (this) {
            if (changingState) {
                throw new IllegalStateException("State change already in progress");
            }
            if (running == false) {
                return;
            }
            changingState = true;
        }
        LOG.info("Shutting down");
        doShutdown();
        LOG.info("Shutdown completed");
        synchronized (this) {
            changingState = false;
            running = false;
            this.notifyAll();
        }
    }
    
    public void awaitShutdown() throws InterruptedException {
        synchronized (this) {
            while (running) {
                wait();
            }
        }
    }

    protected abstract void doShutdown();
    
    protected abstract void doStartup();
}
