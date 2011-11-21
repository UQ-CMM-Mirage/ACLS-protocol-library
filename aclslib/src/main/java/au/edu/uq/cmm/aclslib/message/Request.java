package au.edu.uq.cmm.aclslib.message;

/**
 * This is the interface implemented by all "request" messages.
 * 
 * @author scrawley
 */
public interface Request extends Message {
    /**
     * @return message's request type
     */
    RequestType getType();
}
