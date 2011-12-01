package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.server.Facility;

@SuppressWarnings("serial")
public class AclsLogoutEvent extends AclsFacilityEvent {
    
    public AclsLogoutEvent(Facility source, String userName, String account) {
        super(source, userName, account);
    }

    @Override
    public String toString() {
        return "AclsLogoutEvent [facility=" + getFacility().getFacilityName()
                + ", userName=" + getUserName() + ", account="
                + getAccount() + "]";
    }
}
