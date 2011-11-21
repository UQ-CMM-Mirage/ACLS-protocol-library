package au.edu.uq.cmm.aclslib.message;

import java.util.List;

/**
 * This class represents a FacilityList response containing the list
 * of sub-facilities.
 * 
 * @author scrawley
 */
public class FacilityListResponse extends AbstractResponse {

    private List<String> list;

    /**
     * Construct the response
     * 
     * @param list the facility list
     */
    public FacilityListResponse(List<String> list) {
        super(ResponseType.FACILITY_LIST);
        this.list = list;
    }

    public String unparse() {
        return generateHeader() + FACILITY_DELIMITER + 
               generateList(list, ACCOUNT_SEPARATOR) + DELIMITER;
    }

    /**
     * @return the facility list.
     */
    public List<String> getList() {
        return list;
    }

}
