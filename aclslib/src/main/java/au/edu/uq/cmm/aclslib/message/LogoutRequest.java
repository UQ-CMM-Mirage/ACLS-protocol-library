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
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER)) +
                    generateTrailer(true);
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
