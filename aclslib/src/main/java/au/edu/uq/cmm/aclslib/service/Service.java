package au.edu.uq.cmm.aclslib.service;

public interface Service {
    
    public enum State {
        INITIAL, RUNNING, SHUT_DOWN, FAILED
    }

    void startup() throws ServiceException;
    
    void shutdown() throws ServiceException;
    
    void awaitShutdown() throws InterruptedException;
    
    State getState();
}
