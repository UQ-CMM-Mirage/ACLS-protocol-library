package au.edu.uq.cmm.aclslib.message;

/**
 * This exception indicates that a protocol-level error was detected while
 * attempting to read / parse an ACLS message.
 * 
 * @author scrawley
 */
@SuppressWarnings("serial")
public class AclsProtocolException extends RuntimeException {
    public AclsProtocolException(String message) {
        super(message);
    }

    public AclsProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

}
