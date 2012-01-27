package au.edu.uq.cmm.aclslib.proxy;

import java.util.EventListener;

/**
 * This interface should be implemented by objects that listen for 
 * ACLS Facility events; i.e. ACLS login and logout.
 * 
 * @author scrawley
 */
public interface AclsFacilityEventListener extends EventListener {

    /**
     * This method is called to deliver an event to the listener.
     * @param event the event
     */
    void eventOccurred(AclsFacilityEvent event);
}
