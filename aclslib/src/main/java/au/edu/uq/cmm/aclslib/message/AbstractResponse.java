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

import java.util.List;

/**
 * This is the base class for the different response types.  Note that the leaf 
 * response type classes (in many cases) represent more than one response type
 * to avoid code duplication.
 * 
 * @author scrawley
 */
public abstract class AbstractResponse extends AbstractMessage implements Response {

    private ResponseType type;
    
    /** 
     * Construct the base response 
     * @param type the response type
     */
    AbstractResponse(ResponseType type) {
        this.type = type;
    }
    
    public ResponseType getType() {
        return type;
    }

    /**
     * Unparse the message header for this response.
     * 
     * @return the message header.
     */
    String generateHeader() {
        return type.toString() + COMMAND_DELIMITER;
    }
    
    /**
     * Utility method to unparse the strings in a list with a 
     * terminator after each one.
     * 
     * @param things the list of things to be unparsed
     * @param terminator the terminator string
     * @return the list as text.
     */
    String generateList(List<String> things, String terminator) {
        StringBuilder sb = new StringBuilder();
        for (String thing : things) {
            sb.append(thing);
            sb.append(terminator);
        }
        return sb.toString();
    }
}
