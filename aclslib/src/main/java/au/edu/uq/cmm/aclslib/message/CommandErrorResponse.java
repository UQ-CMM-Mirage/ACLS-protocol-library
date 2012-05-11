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
 * This class represents the response returned by the ACLS server when it
 * detects a bad or meaningless request.
 * 
 * @author scrawley
 */
public class CommandErrorResponse extends AbstractResponse {

    /**
     * Construct the response.
     */
    public CommandErrorResponse() {
        super(ResponseType.COMMAND_ERROR);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader();
    }
}
