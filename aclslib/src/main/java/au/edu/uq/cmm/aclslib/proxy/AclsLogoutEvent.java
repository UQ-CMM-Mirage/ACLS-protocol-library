package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

@SuppressWarnings("serial")
public class AclsLogoutEvent extends AclsFacilityEvent {
    
    private String account;
    
    public AclsLogoutEvent(FacilityConfig source, String userName, String account) {
        super(source, userName);
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "AclsLogoutEvent [facility=" + getFacilityName()
                + ", userName=" + getUserName() + ", account="
                + getAccount() + "]";
    }
}
