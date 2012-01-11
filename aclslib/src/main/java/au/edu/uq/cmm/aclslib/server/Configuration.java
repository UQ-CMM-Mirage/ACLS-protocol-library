package au.edu.uq.cmm.aclslib.server;

import java.net.InetAddress;
import java.util.Collection;

public interface Configuration {

    public abstract int getProxyPort();

    public abstract String getServerHost();

    public abstract int getServerPort();

    public abstract boolean isUseProject();

    public abstract FacilityConfig lookupFacilityByAddress(InetAddress addr);

    public abstract FacilityConfig lookupFacilityById(String id);

    public abstract String getProxyHost();

    public abstract String getDummyFacility();
    
    public abstract Collection<FacilityConfig> getFacilities();

}