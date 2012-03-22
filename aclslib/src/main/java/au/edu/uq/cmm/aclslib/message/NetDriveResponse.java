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
