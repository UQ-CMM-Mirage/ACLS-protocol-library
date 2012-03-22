package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents a general response message with no "payload".
 * 
 * @author scrawley
 */
public class AllowedResponse extends AbstractResponse {

    /**
     * Construct the response object
     * @param type the actual response type
     */
    public AllowedResponse(ResponseType type) {
        super(type);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader();
    }
}
