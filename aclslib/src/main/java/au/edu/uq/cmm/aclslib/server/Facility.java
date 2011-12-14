package au.edu.uq.cmm.aclslib.server;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Facility {
    
    private boolean useFullScreen;
    private String driveName;
    private String accessPassword;
    private String accessName;
    private String folderName;
    private String facilityId;
    private boolean useTimer;
    private String facilityName;
    private boolean dummy;
    private boolean useFileLocks = true;
    private int fileSettlingTime;

    public String getAccessName() {
        return accessName;
    }

    public String getAccessPassword() {
        return accessPassword;
    }

    public String getDriveName() {
        return driveName;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getFolderName() {
        return folderName;
    }

    public boolean isUseFullScreen() {
        return useFullScreen;
    }

    @JsonIgnore
    public boolean isUseNetDrive() {
        return driveName != null;
    }

    public boolean isUseTimer() {
        return useTimer;
    }

    public boolean isDummy() {
        return dummy;
    }

    public void setAccessName(String accessName) {
        this.accessName = accessName;
    }

    public void setAccessPassword(String accessPassword) {
        this.accessPassword = accessPassword;
    }

    public void setDriveName(String driveName) {
        this.driveName = driveName;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setUseFullScreen(boolean useFullScreen) {
        this.useFullScreen = useFullScreen;
    }

    public void setUseTimer(boolean useTimer) {
        this.useTimer = useTimer;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }

    public boolean isUseFileLocks() {
        return this.useFileLocks;
    }

    public void setUseFileLocks(boolean useFileLocks) {
        this.useFileLocks = useFileLocks;
    }
    
    public int getFileSettlingTime() {
        return this.fileSettlingTime;
    }

    public void setFileSettlingTime(int fileSettlingTime) {
        this.fileSettlingTime = fileSettlingTime;
    }
}
