package au.edu.uq.cmm.aclslib.message;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

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
    public SimpleRequest(RequestType type, FacilityConfig facility) {
        super(type, facility);
    }

    public String unparse() {
        return generateHeader();
    }
}
