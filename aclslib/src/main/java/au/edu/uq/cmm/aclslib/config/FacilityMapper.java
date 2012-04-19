package au.edu.uq.cmm.aclslib.config;

import java.net.InetAddress;
import java.util.Collection;

public interface FacilityMapper {
    FacilityConfig lookup(String localHostId, 
            String facilityName, InetAddress clientAddr) 
    throws ConfigurationException;

    Collection<FacilityConfig> allFacilities();
}
