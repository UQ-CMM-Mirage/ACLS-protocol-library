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
 * This class represents a Notes request.  It is not clear if this request 
 * makes any sense in the context of the virtual facility mechanism.
 * 
 * @author scrawley
 */
public class NoteRequest extends AbstractRequest {

    private String userName;
    private String account;
    private String notes;

    /**
     * Construct the message object
     * 
     * @param userName
     * @param account
     * @param notes the notes should be safe for transmission in a 
     *     message.  By convention, embedded newlines should replaced
     *     with semicolons.
     */
    public NoteRequest(String userName, String account, String notes,
            FacilityConfig facility, InetAddress clientAddress, String localHostId) {
        super(RequestType.NOTES, facility, clientAddress, localHostId);
        this.userName = checkName(userName);
        this.account = checkAccount(account);
        this.notes = checkNotes(notes);
    }

    public String unparse(boolean obscurePasswords) {
        return generateHeader() + userName + DELIMITER + 
                ACCOUNT_DELIMITER + account + DELIMITER + 
                NOTE_DELIMITER + notes + DELIMITER +
                generateTrailer(true);
    }

    /**
     * @return the current user's name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the current user's account name
     */
    public String getAccount() {
        return account;
    }

    /**
     * @return the notes.
     */
    public String getNotes() {
        return notes;
    }

}
