package au.edu.uq.cmm.aclslib.proxy;

import java.util.EventObject;

import au.edu.uq.cmm.aclslib.server.FacilityConfig;

@SuppressWarnings("serial")
public abstract class AclsFacilityEvent extends EventObject {
    
    private String userName;
    private String account;

    public AclsFacilityEvent(FacilityConfig source, String userName, String account) {
        super(source);
        this.userName = userName;
        this.account = account;
    }

    public String getUserName() {
        return userName;
    }

    public String getAccount() {
        return account;
    }

    public String getFacilityName() {
        return ((FacilityConfig) getSource()).getFacilityName();
    }
}
