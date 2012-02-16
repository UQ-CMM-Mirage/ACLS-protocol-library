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
    private final Object lock = new Object();

    public void startup() {
        synchronized (lock) {
            if (state == State.STARTING || state == State.STOPPING) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state == State.STARTED) {
                return;
            }
            state = State.STARTING;
        }
        LOG.info("Starting up");
        doStartup();
        LOG.info("Startup completed");
        synchronized (lock) {
            state = State.STARTED;
            lock.notifyAll();
        }
    }

    public void shutdown() throws InterruptedException {
        synchronized (lock) {
            if (state == State.STARTING || state == State.STOPPING) {
                throw new IllegalStateException("State change already in progress");
            }
            if (state != State.STARTED) {
                return;
            }
            state = State.STOPPING;
        }
        LOG.info("Shutting down");
        doShutdown();
        LOG.info("Shutdown completed");
        synchronized (lock) {
            state = State.STOPPED;
            lock.notifyAll();
        }
    }
    
    public void awaitShutdown() throws InterruptedException {
        synchronized (lock) {
            while (state != State.STOPPED) {
                lock.wait();
            }
        }
    }

    public final State getState() {
        synchronized (lock) {
            return state;
        }
    }

    protected abstract void doShutdown() throws InterruptedException;
    
    protected abstract void doStartup();
}
