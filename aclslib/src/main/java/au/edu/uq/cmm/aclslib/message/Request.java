package au.edu.uq.cmm.aclslib.message;

import java.net.InetAddress;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This is the interface implemented by all "request" messages.
 * 
 * @author scrawley
 */
public interface Request extends Message {
    /**
     * @return the message's request type
     */
    RequestType getType();
    
    /**
     * @return the client address for the request message (if it came
     * from an external client), or null.
     */
    InetAddress getClientAddr();
    
    /**
     * @return the (optional) local host Id for the request message or null.
     * (vMFL requests don't have this field, and it is optional for
     * other request types.)
     */
    String getLocalHostId();
    
    /**
     * @return the FacilityConfig for the facility that is presumed to
     * have sent this request.  This could be null, but shouldn't be if 
     * the request was read from a real client.
     */
    FacilityConfig getFacility();
}
