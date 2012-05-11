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
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER)) +
                    generateTrailer(true);
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
