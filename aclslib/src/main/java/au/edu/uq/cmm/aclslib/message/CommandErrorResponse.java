package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents the response returned by the ACLS server when it
 * detects a bad or meaningless request.
 * 
 * @author scrawley
 */
public class CommandErrorResponse extends AbstractResponse {

    /**
     * Construct the response.
     */
    public CommandErrorResponse() {
        super(ResponseType.ERROR);
    }

    public String unparse() {
        return generateHeader();
    }
}
