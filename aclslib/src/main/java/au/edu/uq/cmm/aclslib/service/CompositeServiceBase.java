package au.edu.uq.cmm.aclslib.service;

import org.apache.log4j.Logger;

/**
 * A Service class that extends this class gets infrastructure for starting up and
 * shutting down a "composite" service, comprising other Service instances.  The
 * class needs to implement the {@link #doStartup()} and {@link #doShutdown()}
 * methods.  These typically just call {@link Service#startup()} and 
 * {@link Service#shutdown()} on the component services.
 * 
 * @author scrawley
 */
public abstract class CompositeServiceBase implements Service {
    private static final Logger LOG = Logger.getLogger(CompositeServiceBase.class);
    private State state = State.INITIAL;
    private boolean changingState = false;
    private final Object lock = new Object();

    public void startup() {
        synchronized (lock) {
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
        synchronized (lock) {
            changingState = false;
            state = State.RUNNING;
            lock.notifyAll();
        }
    }

    public void shutdown() {
        synchronized (lock) {
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
        synchronized (lock) {
            changingState = false;
            state = State.SHUT_DOWN;
            lock.notifyAll();
        }
    }
    
    public void awaitShutdown() throws InterruptedException {
        synchronized (lock) {
            while (state != State.SHUT_DOWN) {
                lock.wait();
            }
        }
    }

    public final State getState() {
        synchronized (lock) {
            return state;
        }
    }

    protected abstract void doShutdown();
    
    protected abstract void doStartup();
}
