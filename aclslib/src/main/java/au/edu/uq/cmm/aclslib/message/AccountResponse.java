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

    public String unparse(boolean obscurePasswords) {
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
