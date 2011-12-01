package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.server.Facility;

@SuppressWarnings("serial")
public class AclsLoginEvent extends AclsFacilityEvent {
    
    public AclsLoginEvent(Facility source, String userName, String account) {
        super(source, userName, account);
    }

    @Override
    public String toString() {
        return "AclsLoginEvent [facility=" + getFacility().getFacilityName()
                + ", userName=" + getUserName() + ", account="
                + getAccount() + "]";
    }
}
