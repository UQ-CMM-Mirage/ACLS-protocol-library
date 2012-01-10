package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.server.FacilityConfig;

@SuppressWarnings("serial")
public class AclsLogoutEvent extends AclsFacilityEvent {
    
    public AclsLogoutEvent(FacilityConfig source, String userName, String account) {
        super(source, userName, account);
    }

    @Override
    public String toString() {
        return "AclsLogoutEvent [facility=" + getFacilityId()
                + ", userName=" + getUserName() + ", account="
                + getAccount() + "]";
    }
}
