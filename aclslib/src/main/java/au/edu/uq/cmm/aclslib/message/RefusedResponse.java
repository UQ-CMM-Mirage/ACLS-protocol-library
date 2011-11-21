package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents one of a number of different kinds of "refused"
 * responses.  No addition information is provided.
 * 
 * @author scrawley
 */
public class RefusedResponse extends AbstractResponse {

    public RefusedResponse(ResponseType type) {
        super(type);
    }

    public String unparse() {
        return generateHeader();
    }
}
