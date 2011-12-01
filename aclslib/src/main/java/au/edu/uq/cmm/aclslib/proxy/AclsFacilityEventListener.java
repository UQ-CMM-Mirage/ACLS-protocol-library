package au.edu.uq.cmm.aclslib.proxy;

import java.util.EventListener;

public interface AclsFacilityEventListener extends EventListener {

    void eventOccurred(AclsFacilityEvent event);
}
