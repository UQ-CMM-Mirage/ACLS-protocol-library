package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents one of the two kinds of Logout request.
 * 
 * @author scrawley
 */
public class LogoutRequest extends AbstractRequest {

    private String userName;
    private String password;
    private String account;
    private String facility;

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
            String account, String facility) {
        super(type);
        this.userName = checkName(userName);
        this.password = password == null ? null : checkPassword(password);
        this.account = checkAccount(account);
        this.facility = checkFacility(facility);
    }

    public String unparse() {
        return generateHeader() + userName + DELIMITER + 
                (password == null ? "" : (DELIMITER + password)) +
                ACCOUNT_DELIMITER + account + DELIMITER + 
                (getType() == RequestType.LOGOUT ? "" : 
                    (FACILITY_DELIMITER + facility + DELIMITER));
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
     * @return the name / id of the facility they are logging
     * out of in the virtual case.  In the non-virtual case this
     * property is ignored.
     */
    public String getFacility() {
        return facility;
    }

    /**
     * @return the password of the user logging out.
     */
    public String getPassword() {
        return password;
    }

}
