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
 * This class represents one of the three kinds of login request.  The
 * facility property is only used in the "virtual" login request types.
 * 
 * @author scrawley
 */
public class LoginRequest extends AbstractRequest {

    private String userName;
    private String password;

    /**
     * Construct the message
     * 
     * @param type
     * @param userName
     * @param password
     * @param facility the facility name / identifier or {@literal null}
     */
    public LoginRequest(RequestType type, String userName, 
            String password, FacilityConfig facility, 
            InetAddress clientAddress, String localHostId) {
        super(type, facility, clientAddress, localHostId);
        this.userName = checkName(userName);
        this.password = checkPassword(password);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + userName + DELIMITER + 
                (obscurePasswords ? "XXXXXX" : password) + DELIMITER + 
                (!getType().isVmfl() ? "" : 
                    (FACILITY_DELIMITER + getFacility().getFacilityName() + DELIMITER)) +
                    generateTrailer(true);
    }

    /**
     * @return the user name for the user attempting to login.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the user's password.
     */
    public String getPassword() {
        return password;
    }
}
