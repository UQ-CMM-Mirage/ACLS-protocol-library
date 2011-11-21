package au.edu.uq.cmm.aclslib.message;

/**
 * This exception indicates that a protocol-level error was detected while
 * attempting to read / parse an ACLS message.
 * 
 * @author scrawley
 */
public class AclsProtocolException extends RuntimeException {

    private static final long serialVersionUID = 2955842531448639834L;

    public AclsProtocolException(String message) {
        super(message);
    }

    public AclsProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

}
