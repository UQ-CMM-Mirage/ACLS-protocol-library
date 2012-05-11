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

package au.edu.uq.cmm.aclslib.config;


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
     * Get the registered IP address or DNS name of the facility.
     */
    String getAddress();

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
     * Get the LocalHostID string to be used for the facility.
     */
    String getLocalHostId();

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
}