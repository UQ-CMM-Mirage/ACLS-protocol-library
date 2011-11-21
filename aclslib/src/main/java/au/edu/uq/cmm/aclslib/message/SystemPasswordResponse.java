package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents a SystemPass response, possibly containing 
 * the ACLS system password.
 * 
 * @author scrawley
 */
public class SystemPasswordResponse extends AbstractResponse {

    private String password;

    /**
     * Construct the response
     * 
     * @param password the system password
     */
    public SystemPasswordResponse(String password) {
        super(password == null ? 
                ResponseType.SYSTEM_PASSWORD_NO : 
                ResponseType.SYSTEM_PASSWORD_YES);
        this.password = password == null ? null : checkSystemPassword(password);
    }

    public String unparse() {
        return password == null ?
                generateHeader() :
                (generateHeader() + SYSTEM_PASSWORD_DELIMITER + password + DELIMITER);
    }

    /**
     * @return the system password.
     */
    public String getPassword() {
        return password;
    }

}
