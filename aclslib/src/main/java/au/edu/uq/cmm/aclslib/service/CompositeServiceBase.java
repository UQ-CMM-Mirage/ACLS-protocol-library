package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

public abstract class CompositeServiceBase implements Service {
    private static final Logger LOG = Logger.getLogger(CompositeServiceBase.class);
    private State state = State.INITIAL;
    private boolean changingState = false;

    public void startup() {
        synchronized (this) {
            if (changingState) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state == State.RUNNING) {
                return;
            }
            changingState = true;
        }
        LOG.info("Starting up");
        doStartup();
        LOG.info("Startup completed");
        synchronized (this) {
            changingState = false;
            state = State.RUNNING;
            this.notifyAll();
        }
    }

    public synchronized void shutdown() {
        synchronized (this) {
            if (changingState) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state != State.RUNNING) {
                return;
            }
            changingState = true;
        }
        LOG.info("Shutting down");
        doShutdown();
        LOG.info("Shutdown completed");
        synchronized (this) {
            changingState = false;
            state = State.SHUT_DOWN;
            this.notifyAll();
        }
    }
    
    public void awaitShutdown() throws InterruptedException {
        synchronized (this) {
            while (state != State.SHUT_DOWN) {
                wait();
            }
        }
    }

    public synchronized final State getState() {
        return state;
    }

    protected abstract void doShutdown();
    
    protected abstract void doStartup();
}
