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
 * This enum represents the user's training certification.  The three states are
 * self explanatory.
 * 
 * @author scrawley
 */
public enum Certification {
    VALID("Valid Certificate"), 
    EXPIRED("Expired Certificate"), 
    NONE("No Certificate");
    
    private final String text;

    private Certification(String text) {
        this.text = text;
    }
    
    /**
     * Turn a certificate string from an ACLS message into a Certification value.
     * @param str the ACLS string
     * @return the corresponding Certification
     */
    public static Certification parse(String str) {
        for (Certification value : values()) {
            if (value.text.equalsIgnoreCase(str)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown certification ('" + str + "')");
    }

    @Override
    public String toString() {
        return text;
    }
}
