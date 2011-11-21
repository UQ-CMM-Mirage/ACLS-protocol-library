package au.edu.uq.cmm.aclslib.message;

/**
 * This is the interface implemented by all "response" messages.
 * 
 * @author scrawley
 */
public interface Response extends Message {
    /**
     * @return message's response type
     */
    ResponseType getType();
}
