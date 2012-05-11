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

package au.edu.uq.cmm.aclslib.authenticator;

import java.util.List;

import au.edu.uq.cmm.aclslib.message.Certification;

/**
 * This represents the information returned by ACLS when a user logs in (successfully).
 * 
 * @author scrawley
 *
 */
public class AclsLoginDetails {
    private final String userName;
    private final String humanReadableName;
    private final String orgName;
    private final String password;
    private final String facilityName;
    private final List<String> accounts;
    private final Certification certification;
    private final boolean onsiteAssist;
    private final boolean cached;
    
    public AclsLoginDetails(String userName, String humanReadablName, 
            String orgName, String password, String facilityName, 
            List<String> accounts, Certification certification,
            boolean onsiteAssist, boolean cached) {
        super();
        this.userName = userName;
        this.password = password;
        this.accounts = accounts;
        this.certification = certification;
        this.onsiteAssist = onsiteAssist;
        this.facilityName = facilityName;
        this.orgName = orgName;
        this.humanReadableName = humanReadablName;
        this.cached = cached;
    }

    public String getUserName() {
        return userName;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public Certification getCertification() {
        return certification;
    }

    public boolean isOnsiteAssist() {
        return onsiteAssist;
    }

    public boolean isCached() {
        return cached;
    }
}