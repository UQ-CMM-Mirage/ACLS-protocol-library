package au.edu.uq.cmm.aclslib.message;

import java.net.InetAddress;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This class represents one of the two kinds of Logout request.
 * 
 * @author scrawley
 */
public class LogoutRequest extends AbstractRequest {

    private String userName;
    private String password;
    private String account;

    /**
     * Construct the message.  (Note that a password is required for the
     * vMFL logout requests.)
     * 
     * @param type
     * @param userName
     * @param password
     * @param account
     * @param facility the facility name / identifier or {@literal null}
     */
    public LogoutRequest(RequestType type, String userName, String password,
            String account, FacilityConfig facility, 
            InetAddress clientAddress, String localHostId) {
        super(type, facility, clientAddress, localHostId);
        this.userName = checkName(userName);
        this.password = password == null ? null : checkPassword(password);
        this.account = checkAccount(account);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + userName + DELIMITER + 
                (password == null ? "" : (
                        (obscurePasswords ? "XXXXXX" : password) + DELIMITER)) +
                ACCOUNT_DELIMITER + account + DELIMITER + 
                (!getType().isVmfl() ? "" : 
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER));
    }

    /**
     * @return the name of the user logging out
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * @return the account of the user logging out
     */
    public String getAccount() {
        return account;
    }

    /**
     * @return the password of the user logging out.
     */
    public String getPassword() {
        return password;
    }

}
