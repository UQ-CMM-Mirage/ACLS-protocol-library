package au.edu.uq.cmm.aclslib.proxy;

import java.util.EventObject;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

@SuppressWarnings("serial")
public abstract class AclsFacilityEvent extends EventObject {
    
    private String userName;

    public AclsFacilityEvent(FacilityConfig source, String userName) {
        super(source);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
    
    public String getFacilityName() {
        return ((FacilityConfig) getSource()).getFacilityName();
    }
}
