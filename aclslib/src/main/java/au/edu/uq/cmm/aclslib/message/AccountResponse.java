package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents the 3 variants of "account allowed" response.  The
 * response types mean essentially the same thing.  All include a login 
 * datestamp that may be displayed to the end user.
 * 
 * @author scrawley
 */
public class AccountResponse extends AbstractResponse {
    
    private String loginTimestamp;

    /**
     * Construct the response message
     * 
     * @param type the message type
     * @param loginTimestamp the login timestamp
     */
    public AccountResponse(ResponseType type, String loginTimestamp) {
        super(type);
        this.loginTimestamp = loginTimestamp;
    }

    public String unparse() {
        return generateHeader() + TIME_DELIMITER + loginTimestamp + DELIMITER;
    }

    /**
     * The purpose of the login timestamp is to tell the end user when
     * he / she logged in ... in the time frame of the ACLS server.  
     * (The system clock on a "facility" may be wildly inaccurate!)
     * 
     * @return the login timestamp in an unspecified format.
     */
    public String getLoginTimestamp() {
        return loginTimestamp;
    }
}
