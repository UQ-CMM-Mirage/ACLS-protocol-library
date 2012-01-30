package au.edu.uq.cmm.aclslib.config;

import java.util.List;

/**
 * Configuration details API for a proxied ACLS facility.  Some
 * of these properties configure the behavior of the proxy and the
 * grabber.  Others are passed to the ACLS facility itself (via
 * the protocol).
 * <p>
 * (The names of some of these properties reflect the sometimes
 * strange terminology used by ACLS and its documentation.)
 * 
 * @author scrawley
 */
public interface FacilityConfig {

    /**
     * Get the user name that the Facility should use when mounting
     * its shared drive.
     */
    String getAccessName();

    /**
     * Get the password that the Facility should use when mounting
     * its shared drive.
     */
    String getAccessPassword();

    /**
     * Get the drive name (e.g. "X") that the Facility should use 
     * to mount its shared drive.
     */
    String getDriveName();

    /**
     * Get the name of the facility
     */
    String getFacilityName();

    /**
     * Get the long-form description of the facility.  (ACLS calls 
     * this the name)
     */
    String getFacilityDescription();

    /**
     * Get the remote "folder name" that the Facility should attempt
     * to mount.  This should be a UNC name of an SMB share; e.g. 
     * "\\host\share".
     */
    String getFolderName();
    
    /**
     * Get the datafile template configurations for the datafiles in a dataset. 
     */
    List<? extends DatafileTemplateConfig> getDatafileTemplates();

    /**
     * If true, tell the client to grab the screen when no use is
     * logged in.
     */
    boolean isUseFullScreen();

    /**
     * This is a derived property.  Should be true if the drive name
     * is non-empty.
     */
    boolean isUseNetDrive();

    /**
     * If true, tell the client to start a logout timer.
     */
    boolean isUseTimer();

    /**
     * If true, this Facility is the dummy (virtual) facility to be 
     * used for username / password checking; e.g. using Benny.
     */
    boolean isDummy();

    /**
     * If true, the file grabber should acquire a file lock before copying
     * (grabbing) a file from this facility's shared drive area.
     */
    boolean isUseFileLocks();

    /**
     * The file settling time is the time (in milliseconds) to wait after 
     * the last file modification event before the grabber attempts to grab the file.
     */
    int getFileSettlingTime();

    /**
     * Get the registered IP address or DNS name of the facility.
     */
    String getAddress();
    
}