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
