package au.edu.uq.cmm.aclslib.message;

@SuppressWarnings("serial")
public class AclsNoResponseException extends AclsCommsException {

    public AclsNoResponseException(String message, Throwable ex) {
        super(message, ex);
    }

    public AclsNoResponseException(String message) {
        super(message);
    }

}
