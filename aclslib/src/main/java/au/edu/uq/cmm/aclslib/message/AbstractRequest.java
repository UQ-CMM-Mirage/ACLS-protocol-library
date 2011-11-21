package au.edu.uq.cmm.aclslib.message;

/**
 * This is the base class for the different request types.  Note that the leaf 
 * request type classes (in many cases) represent more than one request type
 * to avoid code duplication.
 * 
 * @author scrawley
 */
public abstract class AbstractRequest extends AbstractMessage implements Request {

    private RequestType type;
    
    /** 
     * Construct the base request 
     * @param type the request type
     */
    AbstractRequest(RequestType type) {
        this.type = type;
    }
    
    public RequestType getType() {
        return type;
    }

    /**
     * Unparse the message header for this request.
     * 
     * @return the message header.
     */
    String generateHeader() {
        return type.toString() + COMMAND_DELIMITER;
    }
}
