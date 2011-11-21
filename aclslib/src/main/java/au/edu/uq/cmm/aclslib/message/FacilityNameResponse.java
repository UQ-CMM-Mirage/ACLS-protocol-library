package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents a FacilityAllowed response containing the name
 * of the current facility.
 * 
 * @author scrawley
 */
public class FacilityNameResponse extends AbstractResponse {

    private String facility;

    /**
     * Construct the response
     * 
     * @param facility the facility name
     */
    public FacilityNameResponse(String facility) {
        super(ResponseType.FACILITY_ALLOWED);
        this.facility = checkFacility(facility);
    }

    public String unparse() {
        return generateHeader() + FACILITY_DELIMITER + facility + DELIMITER;
    }

    /**
     * @return the facility name.
     */
    public String getFacility() {
        return facility;
    }

}
