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

/**
 * This enum represents the ACLS request types, and handles the mapping
 * between the types and their respective code / command numbers.  Refer to
 * the ACLS protocol document for the details of what the various types mean.
 * 
 * @author scrawley
 */
public enum RequestType {
    LOGIN(1, false, true), 
    LOGOUT(2, false, true), 
    ACCOUNT(3, false, true), 
    NOTES(4, false, true), 
    FACILITY_NAME(5, false, true), 
    USE_PROJECT(6, false),
    USE_TIMER(7, false), 
    USE_VIRTUAL(8, true),
    FACILITY_COUNT(9, true),
    FACILITY_LIST(10, true),
    VIRTUAL_LOGIN(11, true),
    VIRTUAL_LOGOUT(12, true),
    VIRTUAL_ACCOUNT(13, true),
    NEW_VIRTUAL_LOGIN(14, true), 
    NEW_VIRTUAL_ACCOUNT(15, true), 
    SYSTEM_PASSWORD(20, false), 
    STAFF_LOGIN(21, false), 
    NET_DRIVE(22, false), 
    USE_FULL_SCREEN(23, false);
    
    private final int code;
    private final boolean vmfl;
    private final boolean sendLocalHostId;
    
    RequestType(int code, boolean vmfl, boolean sendLocalHostId) {
        this.code = code;
        this.vmfl = vmfl;
        this.sendLocalHostId = sendLocalHostId;
    }
    
    RequestType(int code, boolean vmfl) {
        this(code, vmfl, false);
    }
    
    public int getCode() {
        return code;
    }
    
    public boolean isVmfl() {
        return vmfl;
    }
    
    public boolean isLocalHostIdAllowed() {
        return sendLocalHostId;
    }

    public static RequestType parse(String str) {
        try {
            int code = Integer.parseInt(str);
            for (RequestType val : values()) {
                if (val.getCode() == code) {
                    return val;
                }
            }
            throw new IllegalArgumentException("Unknown request type ('" + str + "')");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Request type not a number ('" + str + "')");
        }
    }
    
    public String toString() {
        return "" + code;
    }
}
