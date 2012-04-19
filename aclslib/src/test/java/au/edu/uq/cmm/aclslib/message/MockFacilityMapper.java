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
