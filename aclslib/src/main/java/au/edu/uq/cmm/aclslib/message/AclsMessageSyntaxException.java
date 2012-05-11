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
 * This exception is thrown if there is a syntax error in a message
 * that we are attempting to read.
 * 
 * @author scrawley
 */
@SuppressWarnings("serial")
public class AclsMessageSyntaxException extends AclsProtocolException {
    public AclsMessageSyntaxException(String msg) {
        super(msg);
    }

    public AclsMessageSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

}
