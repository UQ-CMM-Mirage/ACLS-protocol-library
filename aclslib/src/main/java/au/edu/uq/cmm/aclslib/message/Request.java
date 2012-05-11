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
