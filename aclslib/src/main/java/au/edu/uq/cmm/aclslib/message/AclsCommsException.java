package au.edu.uq.cmm.aclslib.message;

public class AclsCommsException extends AclsProtocolException {

    private static final long serialVersionUID = -5458511312056105338L;

    public AclsCommsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AclsCommsException(String message) {
        super(message);
    }

}
