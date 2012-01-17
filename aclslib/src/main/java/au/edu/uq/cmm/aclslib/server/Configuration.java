package au.edu.uq.cmm.aclslib.server;

import java.net.InetAddress;
import java.util.Collection;

public interface Configuration {

    int getProxyPort();

     String getServerHost();

     int getServerPort();

     boolean isUseProject();

     FacilityConfig lookupFacilityByAddress(InetAddress addr);

     FacilityConfig lookupFacilityById(String id);

     String getProxyHost();

     String getDummyFacility();
    
     Collection<FacilityConfig> getFacilities();
    
     String getBaseFileUrl();
     
     String getCaptureDirectory();
     
     String getFeedId();
     
     String getFeedTitle();
     
     String getFeedAuthor();
     
     String getFeedAuthorEmail();

}