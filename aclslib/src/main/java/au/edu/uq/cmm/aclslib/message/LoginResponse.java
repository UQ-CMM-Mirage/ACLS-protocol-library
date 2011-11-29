package au.edu.uq.cmm.aclslib.message;

import java.util.List;

/**
 * This class represents the three kinds of successful login response.  It gives
 * the logged in user's name and organization, a list of accounts to select from,
 * the user's training certification and the "onsite assistance" flag that 
 * typically indicates that the user needs a staff member to train them.
 * 
 * @author scrawley
 */
public class LoginResponse extends AbstractResponse {

    private String userName;
    private String orgName;
    private Certification certification;
    private boolean onsiteAssist;
    private List<String> accounts;

    /**
     * Construct the response message
     * 
     * @param type
     * @param userName
     * @param orgName
     * @param accounts
     * @param certification
     * @param onsiteAssist
     */
    public LoginResponse(ResponseType type, String userName, String orgName, 
            List<String> accounts, Certification certification, boolean onsiteAssist) {
        super(type);
        this.userName = checkName(userName);
        this.orgName = checkOrganization(orgName);
        this.accounts = accounts;
        for (String acc : accounts) {
            checkAccount(acc);
        }
        this.certification = certification;
        this.onsiteAssist = onsiteAssist;
    }

    public String unparse() {
        return generateHeader() + userName + DELIMITER + orgName + DELIMITER + 
                ACCOUNT_DELIMITER + generateList(accounts, ACCOUNT_SEPARATOR) + 
                DELIMITER + CERTIFICATE_DELIMITER + certification + 
                ONSITE_ASSIST_DELIMITER + 
                (onsiteAssist ? AbstractMessage.YES : AbstractMessage.NO) + 
                DELIMITER;
    }

    /**
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the user's organization
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * @return the user's certification
     */
    public Certification getCertification() {
        return certification;
    }

    /**
     * @return {@literal true} if the user requires staff assistance or training.
     */
    public boolean isOnsiteAssist() {
        return onsiteAssist;
    }

    /**
     * @return a list of the accounts that the user can select from for
     * accounting / billing purposes.
     */
    public List<String> getAccounts() {
        return accounts;
    }

}
