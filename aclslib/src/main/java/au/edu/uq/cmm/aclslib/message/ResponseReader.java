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

import java.io.InputStream;

/**
 * This is the API for an ACLS response reader.
 * 
 * @author scrawley
 */
public interface ResponseReader {

    /**
     * Read and parse a response from the supplied input stream
     * 
     * @param source the input stream
     * @return the response message, if parsing was successful.
     * @throws AclsException 
     */
    Response read(InputStream source) throws AclsException;
    
    /**
     * Read and parse status line followed by a response from the supplied input stream
     * 
     * @param source the input stream
     * @return the response message, if parsing was successful.
     * @throws AclsException 
     */
    Response readWithStatusLine(InputStream source) throws AclsException;
}
