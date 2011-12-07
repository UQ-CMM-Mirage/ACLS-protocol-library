package au.edu.uq.cmm.aclslib.message;

/**
 * This exception is thrown if there is a syntax error in a message
 * that we are attempting to read.
 * 
 * @author scrawley
 */
@SuppressWarnings("serial")
public class MessageSyntaxException extends AclsProtocolException {
    public MessageSyntaxException(String msg) {
        super(msg);
    }

    public MessageSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}
