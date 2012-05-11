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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;

public class MockFacilityMapper implements FacilityMapper {

    private List<StaticFacilityConfig> facilities;

    public MockFacilityMapper(List<StaticFacilityConfig> facilities) {
        this.facilities = facilities;
    }

    public FacilityConfig lookup(String localHostId, String facilityName,
            InetAddress clientAddr) {
        if (localHostId != null) {
            for (FacilityConfig facility : facilities) {
                if (localHostId.equals(facility.getLocalHostId())) {
                    return facility;
                }
            }
        }
        if (facilityName != null) {
            for (FacilityConfig facility : facilities) {
                if (facilityName.equals(facility.getFacilityName())) {
                    return facility;
                }
            }
        }
        if (clientAddr != null) {
            for (FacilityConfig facility : facilities) {
                try {
                    if (clientAddr.equals(InetAddress.getByName(facility.getAddress()))) {
                        return facility;
                    }
                } catch (UnknownHostException ex) {
                    throw new AssertionError(ex);
                }
            }
        }
        return null;
    }

    public Collection<FacilityConfig> allFacilities() {
        return new ArrayList<FacilityConfig>(facilities);
    }

}
