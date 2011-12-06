package au.edu.uq.cmm.aclslib.service;

import java.io.IOException;

@SuppressWarnings("serial")
public class ServiceException extends RuntimeException {
    public ServiceException(String msg, IOException ex) {
        super(msg, ex);
    }

    public ServiceException(String msg) {
        super(msg);
    }
}
