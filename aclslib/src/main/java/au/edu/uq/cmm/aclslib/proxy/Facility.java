package au.edu.uq.cmm.aclslib.proxy;

public class Facility {
    
    private boolean useFullScreen;
    private String driveName;
    private String accessPassword;
    private String accessName;
    private String folderName;
    private String facilityId;
    private boolean useTimer;
    private String facilityName;

    public boolean isUseFullscreen() {
        return useFullScreen;
    }

    public boolean isUseNetDrive() {
        return driveName != null;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getDriveName() {
        return driveName;
    }

    public String getAccessPassword() {
        return accessPassword;
    }

    public String getAccessName() {
        return accessName;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public boolean isUseTimer() {
        return useTimer;
    }

    public String getFacilityName() {
        return facilityName;
    }

}
