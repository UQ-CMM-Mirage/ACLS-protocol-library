package au.edu.uq.cmm.aclslib.message;

import java.net.InetAddress;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This class represents one of the three kinds of account request.  These
 * requests select the account that the user is going to use from the list
 * provided in the login response.
 * 
 * @author scrawley
 */
public class AccountRequest extends AbstractRequest {

    private String userName;
    private String account;

    /**
     * Create an account request.
     * 
     * @param type the request type
     * @param userName the user's name
     * @param account the user's selected account
     * @param facility the facility name / id (for the virtual cases) 
     *        or {@literal null}.
     */
    public AccountRequest(RequestType type, String userName, 
            String account, FacilityConfig facility, 
            InetAddress clientAddress, String localHostId) {
        super(type, facility, clientAddress, localHostId);
        this.userName = userName;
        this.account = account;
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + userName + DELIMITER + 
                ACCOUNT_DELIMITER + account + DELIMITER + 
                (!getType().isVmfl() ? "" : 
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER));
    }

    /**
     * @return The request's user name.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * @return The request's account name.
     */
    public String getAccount() {
        return account;
    }
}
