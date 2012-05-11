/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.message;

import java.net.InetAddress;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This is the base class for the different request types.  Note that the leaf 
 * request type classes (in many cases) represent more than one request type
 * to avoid code duplication.
 * 
 * @author scrawley
 */
public abstract class AbstractRequest extends AbstractMessage implements Request {

    private RequestType type;
    private FacilityConfig facility;
    private InetAddress clientAddress;
    private String localHostId;
    
    /** 
     * Construct the base request 
     * @param type the request type
     * @param facility the specified or inferred facility this message comes from
     * @param clientAddress the source IP address
     * @param localHostId the localHostId provided in the message
     */
    AbstractRequest(RequestType type, FacilityConfig facility, 
            InetAddress clientAddress, String localHostId) {
        this.type = type;
        this.facility = facility;
        this.clientAddress = clientAddress;
        this.localHostId = localHostId;
    }
    
    public final RequestType getType() {
        return type;
    }

    public final FacilityConfig getFacility() {
        return facility;
    }

    public final InetAddress getClientAddr() {
        return clientAddress;
    }

    public final String getLocalHostId() {
        return localHostId;
    }

    /**
     * Unparse the message header for this request.
     * 
     * @return the message header.
     */
    String generateHeader() {
        return type.toString() + COMMAND_DELIMITER;
    }
    
    /**
     * Unparse the message trailer for this request.  For request types that
     * allow it, we include a localHostId if we have one.  We use the localHostId
     * from the request if it was provided, and fall back to the localHostId
     * from the facility descriptor. 
     * 
     * @return the message trailer.
     */
    String generateTrailer(boolean withCommandDelimiter) {
        if (type.isVmfl() || !type.isLocalHostIdAllowed()) {
            return "";
        }
        String id = localHostId;
        if ((id == null || id.isEmpty()) && facility != null) {
            id = facility.getLocalHostId();
        } 
        if (id == null || id.isEmpty()) {
            return "";
        } else if (withCommandDelimiter) {
            return COMMAND_DELIMITER + id + DELIMITER;
        } else {
            return id + DELIMITER;
        }
    }
}
