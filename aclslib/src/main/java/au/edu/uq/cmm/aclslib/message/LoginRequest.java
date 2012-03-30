package au.edu.uq.cmm.aclslib.message;

import java.net.InetAddress;

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
            String password, FacilityConfig facility, 
            InetAddress clientAddress, String localHostId) {
        super(type, facility, clientAddress, localHostId);
        this.userName = checkName(userName);
        this.password = checkPassword(password);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + userName + DELIMITER + 
                (obscurePasswords ? "XXXXXX" : password) + DELIMITER + 
                (!getType().isVmfl() ? "" : 
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER)) +
                    generateTrailer();
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
