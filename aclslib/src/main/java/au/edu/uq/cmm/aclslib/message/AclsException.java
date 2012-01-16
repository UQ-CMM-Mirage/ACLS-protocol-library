package au.edu.uq.cmm.aclslib.message;

public class AclsException extends Exception {

    private static final long serialVersionUID = -6239422654331656058L;

    public AclsException(String message) {
        super(message);
    }

    public AclsException(String message, Throwable cause) {
        super(message, cause);
    }

}
