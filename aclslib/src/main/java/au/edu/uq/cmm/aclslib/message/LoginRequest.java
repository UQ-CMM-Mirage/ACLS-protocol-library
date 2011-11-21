package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents one of the three kinds of login request.  The
 * facility property is only used in the "virtual" login request types.
 * 
 * @author scrawley
 */
public class LoginRequest extends AbstractRequest {

    private String userName;
    private String password;
    private String facility;

    /**
     * Construct the message
     * 
     * @param type
     * @param userName
     * @param password
     * @param facility the facility name / identifier or {@literal null}
     */
    public LoginRequest(RequestType type, String userName, 
            String password, String facility) {
        super(type);
        this.userName = checkName(userName);
        this.password = checkPassword(password);
        this.facility = checkFacility(facility);
    }

    public String unparse() {
        return generateHeader() + userName + DELIMITER + password + DELIMITER + 
                (getType() == RequestType.LOGIN ? "" : 
                    (FACILITY_DELIMITER + facility + DELIMITER));
    }

    /**
     * @return the user name for the user attempting to login.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the facility name or id, or {@literal null} for the
     * non-virtual case.
     */
    public String getFacility() {
        return facility;
    }

}
