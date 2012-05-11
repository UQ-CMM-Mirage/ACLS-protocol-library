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
 * This class represents a NetDrive response, containing the parameters 
 * needed to mount a (Windows) network drive.
 * 
 * @author scrawley
 */
public class NetDriveResponse extends AbstractResponse {

    private String driveName;
    private String folderName;
    private String accessName;
    private String accessPassword;

    /**
     * Construct a yes response
     * 
     * @param driveName the drive name
     * @param folderName the folder name
     * @param accessName the access name
     * @param accessPassword the access password
     */
    public NetDriveResponse(String driveName, String folderName, 
            String accessName, String accessPassword) {
        super(ResponseType.NET_DRIVE_YES);
        this.driveName = checkDrive(driveName);
        this.folderName = checkFolderName(folderName);
        this.accessName = checkAccessName(accessName);
        this.accessPassword = checkAccessPassword(accessPassword);
    }
    
    /**
     * Construct a no response
     */
    public NetDriveResponse() {
        super(ResponseType.NET_DRIVE_NO);
    }

    public String unparse(boolean obscurePasswords) {
        if (getType() == ResponseType.NET_DRIVE_NO) {
            return generateHeader();
        } else {
            return generateHeader() + driveName + ACCOUNT_DELIMITER + 
                    folderName + TIME_DELIMITER + accessName + 
                    ONSITE_ASSIST_DELIMITER + 
                    (obscurePasswords ? "XXXXXX" : accessPassword) + DELIMITER;
        }
    }

    /**
     * @return the network drive name.  If this is empty, then
     * the client will treat this as meaning that no drive mount
     * is required.  
     */
    public String getDriveName() {
        return driveName;
    }

    /**
     * @return the name of the remote folder to be mounted
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * @return the access name for mounting the folder
     */
    public String getAccessName() {
        return accessName;
    }

    /**
     * @return the password corresponding to the access name
     */
    public String getAccessPassword() {
        return accessPassword;
    }

}
