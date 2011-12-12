package au.edu.uq.cmm.aclslib.service;

/**
 * Light-weight API for component "services" supporting the notion
 * of startup and shutdown.
 * 
 * @author scrawley
 */
public interface Service {
    
    /**
     * This represents the possible states of a service.
     * 
     * @author scrawley
     */
    public enum State {
        /** The service has never been started */
        INITIAL, 
        /** The service is currently running (as far as we can tell) */
        RUNNING, 
        /** The service has been shut down. */
        SHUT_DOWN, 
        /** The service has failed. */
        FAILED
    }

    /** 
     * Start the service.  If the service is already running, this is a no-op.
     * A service in shutdown or failed state can typically be (re-)started.
     */
    void startup() throws ServiceException;
    
    /** 
     * Shutdown the service.  If the service is already shutdown or has never
     * been started, this is a no-op.  If the service is failed, this moves it
     * to the shutdown state.
     * 
     * @throws ServiceException
     */
    void shutdown() throws ServiceException;
    
    /**
     * Wait for the service to enter the shutdown state; e.g. in response to
     * a {@link #shutdown()} call made by a different thread.
     * 
     * @throws InterruptedException
     */
    void awaitShutdown() throws InterruptedException;
    
    /**
     * Get the service's current state.
     * 
     * @return a "best effort" version of the services state.  In some cases,
     * the FAILED state may not be detectable.
     */
    State getState();
}
