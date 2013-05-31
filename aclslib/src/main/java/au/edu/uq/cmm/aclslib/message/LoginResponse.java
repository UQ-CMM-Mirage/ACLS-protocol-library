/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.message;

import java.util.List;
import java.util.Objects;

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
    private String loginTimestamp;

    /**
     * Construct the response message
     * 
     * @param type
     * @param userName
     * @param orgName
     * @param loginTimestamp - this will be null for a classic login response.
     * @param accounts
     * @param certification
     * @param onsiteAssist
     */
    public LoginResponse(ResponseType type, String userName, String orgName, 
            String loginTimestamp, List<String> accounts, 
            Certification certification, boolean onsiteAssist) {
        super(type);
        this.userName = checkName(userName);
        this.orgName = checkOrganization(orgName);
        this.loginTimestamp = loginTimestamp; // may be be null
        this.accounts = accounts;
        for (String acc : accounts) {
            checkAccount(acc);
        }
        this.certification = Objects.requireNonNull(certification);
        this.onsiteAssist = onsiteAssist;
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + userName + DELIMITER + orgName + DELIMITER + 
                (loginTimestamp == null ? "" : (TIME_DELIMITER + loginTimestamp) + DELIMITER) +
                ACCOUNT_DELIMITER + generateList(accounts, ACCOUNT_SEPARATOR) + 
                DELIMITER + CERTIFICATE_DELIMITER + certification + 
                ONSITE_ASSIST_DELIMITER + 
                (onsiteAssist ? AbstractMessage.YES : "") + 
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

    /**
     * @return the login timestamp or {@literal null}.
     */
    public String getLoginTimestamp() {
        return loginTimestamp;
    }
}
