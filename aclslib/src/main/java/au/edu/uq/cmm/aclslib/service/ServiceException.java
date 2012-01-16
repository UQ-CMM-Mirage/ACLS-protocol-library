package au.edu.uq.cmm.aclslib.service;

import java.io.IOException;


public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 4852028842984971626L;

    public ServiceException(String msg, IOException ex) {
        super(msg, ex);
    }

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
