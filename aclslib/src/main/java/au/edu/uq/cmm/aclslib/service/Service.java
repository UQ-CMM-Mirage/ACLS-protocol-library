package au.edu.uq.cmm.aclslib.service;

public interface Service {

    void startup() throws ServiceException;
    
    void shutdown() throws ServiceException;
    
    void awaitShutdown() throws InterruptedException;
}
