package au.edu.uq.cmm.aclslib.message;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This class represents one of the three kinds of login request.  The
 * facility property is only used in the "virtual" login request types.
 * 
 * @author scrawley
 */
public class LoginRequest extends AbstractRequest {

    private String userName;
    private String password;

    /**
     * Construct the message
     * 
     * @param type
     * @param userName
     * @param password
     * @param facility the facility name / identifier or {@literal null}
     */
    public LoginRequest(RequestType type, String userName, 
            String password, FacilityConfig facility) {
        super(type, facility);
        this.userName = checkName(userName);
        this.password = checkPassword(password);
    }

    public String unparse() {
        return generateHeader() + userName + DELIMITER + password + DELIMITER + 
                (!getType().isVmfl() ? "" : 
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER));
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
}
