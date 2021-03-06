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

    public String unparse(boolean obscurePasswords) {
        if (password == null) {
            return generateHeader();
        } else {
            return generateHeader() + SYSTEM_PASSWORD_DELIMITER + 
                    (obscurePasswords ? "XXXXXX" : password) + DELIMITER;
        }
    }

    /**
     * @return the system password.
     */
    public String getPassword() {
        return password;
    }

}
