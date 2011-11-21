package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents a parameterless ACLS request.
 * 
 * @author scrawley
 */
public class SimpleRequest extends AbstractRequest {

    /**
     * Construct the request.
     * 
     * @param type
     */
    public SimpleRequest(RequestType type) {
        super(type);
    }

    public String unparse() {
        return generateHeader();
    }
}
