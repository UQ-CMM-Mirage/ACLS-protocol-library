package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;

@SuppressWarnings("serial")
public class AclsPasswordAcceptedEvent extends AclsFacilityEvent {
    
    private AclsLoginDetails loginDetails;

    public AclsPasswordAcceptedEvent(FacilityConfig facility, AclsLoginDetails loginDetails) {
        super(facility, loginDetails.getUserName());
        this.loginDetails = loginDetails;
    }

    public AclsLoginDetails getLoginDetails() {
        return loginDetails;
    }

    @Override
    public String toString() {
        return "AclsPasswordAcceptedEvent [facility=" + getFacilityName() +
                ", userName=" + getUserName() + 
                ", details=" + getLoginDetails() + "]";
    }
}
