package au.edu.uq.cmm.aclslib.authenticator;


import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsException;

public interface Authenticator {
    AclsLoginDetails authenticate(String userName, String password, FacilityConfig facility) 
            throws AclsException;
}
