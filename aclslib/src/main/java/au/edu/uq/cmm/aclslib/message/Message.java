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
 * The base interface for ACLS message classes.  It should be noted that 
 * the use-cases for these classes mean that we need to reflect some 
 * aspects of the protocol that are not best-practice in protocol design.
 * We had to choose between clean modeling and lots of complex code to 
 * map from the model to reality (and back), or an unclean (leaky) model.
 * We chose the latter, in the anticipation that the protocol will be
 * redesigned from the ground up at some point.
 * 
 * @author scrawley
 */
public interface Message {
    
    /**
     * Turn this Message into a textual ACLS message.
     * 
     * @param obscurePasswords if true, replace any password field with some XXX's.
     * @return the message in text form.
     */
    String unparse(boolean obscurePasswords);
}
