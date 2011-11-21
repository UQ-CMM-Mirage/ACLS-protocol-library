package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents a FacilityCount response containing the number
 * of sub-facilities.
 * 
 * @author scrawley
 */
public class FacilityCountResponse extends AbstractResponse {

    private int count;

    /**
     * Construct the response
     * 
     * @param count the facility count
     */
    public FacilityCountResponse(int count) {
        super(ResponseType.FACILITY_COUNT);
        this.count = count;
    }

    public String unparse() {
        return generateHeader() + FACILITY_DELIMITER + count + DELIMITER;
    }

    /**
     * @return the facility count.
     */
    public int getCount() {
        return count;
    }

}
