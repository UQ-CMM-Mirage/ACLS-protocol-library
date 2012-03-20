package au.edu.uq.cmm.aclslib.message;

import java.net.InetAddress;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This class represents a parameterless ACLS request.
 * 
 * @author scrawley
 */
public class SimpleRequest extends AbstractRequest {

    /**
     * Construct the request.
     * 
     * @param type
     */
    public SimpleRequest(RequestType type, FacilityConfig facility, 
            InetAddress clientAddress, String localHostId) {
        super(type, facility, clientAddress, localHostId);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader();
    }
}
