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
 * This class represents a FacilityCount response containing the number
 * of sub-facilities.
 * 
 * @author scrawley
 */
public class FacilityCountResponse extends AbstractResponse {

    private int count;

    /**
     * Construct the response
     * 
     * @param count the facility count
     */
    public FacilityCountResponse(int count) {
        super(ResponseType.FACILITY_COUNT);
        this.count = count;
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + FACILITY_DELIMITER + count + DELIMITER;
    }

    /**
     * @return the facility count.
     */
    public int getCount() {
        return count;
    }

}
