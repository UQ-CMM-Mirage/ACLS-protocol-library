package au.edu.uq.cmm.aclslib.server;

/**
 * Configuration details API for a proxied facility.
 * 
 * @author scrawley
 */
public interface FacilityConfig {

    public abstract String getAccessName();

    public abstract String getAccessPassword();

    public abstract String getDriveName();

    public abstract String getFacilityId();

    public abstract String getFacilityName();

    public abstract String getFolderName();

    public abstract boolean isUseFullScreen();

    public abstract boolean isUseNetDrive();

    public abstract boolean isUseTimer();

    public abstract boolean isDummy();

    public abstract boolean isUseFileLocks();

    public abstract int getFileSettlingTime();

}