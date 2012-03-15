package au.edu.uq.cmm.aclslib.message;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This is the base class for the different request types.  Note that the leaf 
 * request type classes (in many cases) represent more than one request type
 * to avoid code duplication.
 * 
 * @author scrawley
 */
public abstract class AbstractRequest extends AbstractMessage implements Request {

    private RequestType type;
    private FacilityConfig facility;
    
    /** 
     * Construct the base request 
     * @param type the request type
     */
    AbstractRequest(RequestType type, FacilityConfig facility) {
        this.type = type;
        this.facility = facility;
    }
    
    public RequestType getType() {
        return type;
    }

    public FacilityConfig getFacility() {
        return facility;
    }

    /**
     * Unparse the message header for this request.
     * 
     * @return the message header.
     */
    String generateHeader() {
        return type.toString() + COMMAND_DELIMITER;
    }
    
    /**
     * Unparse the message trailer for this request.
     * 
     * @return the message trailer.
     */
    String generateTrailer() {
        if (type.isVmfl() || !type.isLocalHostIdAllowed()) {
            return "";
        } else if (facility == null || facility.getLocalHostId().isEmpty()) {
            return "";
        } else {
            return COMMAND_DELIMITER + facility.getLocalHostId() + DELIMITER;
        }
    }
}
