package au.edu.uq.cmm.aclslib.message;

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
    private String facility;

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
            String account, String facility) {
        super(type);
        this.userName = userName;
        this.account = account;
        this.facility = facility;
    }

    public String unparse() {
        return generateHeader() + userName + DELIMITER + 
                ACCOUNT_DELIMITER + account + DELIMITER + 
                (getType() == RequestType.ACCOUNT ? "" : 
                    (FACILITY_DELIMITER + facility + DELIMITER));
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

    /**
     * @return The request's facility name / id.
     */
    public String getFacility() {
        return facility;
    }

}
