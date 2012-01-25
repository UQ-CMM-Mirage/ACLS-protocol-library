package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

@SuppressWarnings("serial")
public class AclsLoginEvent extends AclsFacilityEvent {
    
    public AclsLoginEvent(FacilityConfig source, String userName, String account) {
        super(source, userName, account);
    }

    @Override
    public String toString() {
        return "AclsLoginEvent [facility=" + getFacilityName()
                + ", userName=" + getUserName() + ", account="
                + getAccount() + "]";
    }
}
