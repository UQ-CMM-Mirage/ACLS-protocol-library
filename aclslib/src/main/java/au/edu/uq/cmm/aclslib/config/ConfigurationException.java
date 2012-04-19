package au.edu.uq.cmm.aclslib.config;

import au.edu.uq.cmm.aclslib.message.AclsException;

public class ConfigurationException extends AclsException {
    
    private static final long serialVersionUID = 3931257231683284811L;

    public ConfigurationException(String message, Throwable ex) {
        super(message, ex);
    }

    public ConfigurationException(String message) {
        super(message);
    }
}
