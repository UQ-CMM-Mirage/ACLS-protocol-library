package au.edu.uq.cmm.aclslib.message;

/**
 * This exception is thrown if there is a syntax error in a message
 * that we are attempting to read.
 * 
 * @author scrawley
 */
public class MessageSyntaxException extends AclsProtocolException {
    private static final long serialVersionUID = 1642749026917856081L;

    public MessageSyntaxException(String msg) {
        super(msg);
    }

    public MessageSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}
