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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ConfigurationException;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;

/**
 * This class is an ACLS request reader for versions 20.x and 30.x of the 
 * ACLS protocol.
 * 
 * @author scrawley
 */
public class RequestReaderImpl extends AbstractReader implements RequestReader {
    private static final Logger LOG = LoggerFactory.getLogger(RequestReaderImpl.class);
    
    private FacilityMapper facilityMapper;
    private InetAddress clientAddr;
    private FacilityConfig facility;
    private String facilityName;
    private String localHostId;
    
    public RequestReaderImpl(FacilityMapper facilityMapper, InetAddress clientAddr) {
        super(LOG, true);
        this.facilityMapper = facilityMapper;
        this.clientAddr = clientAddr;
    }

    public Request read(InputStream source) throws AclsException {
        Scanner scanner;
        try {
            scanner = createLineScanner(
                new BufferedReader(new InputStreamReader(source)));
        } catch (IOException ex) {
            throw new AclsCommsException("IO error with reading request", ex);
        }
        String command;
        try {
            command = scanner.next();
        } catch (NoSuchElementException ex) {
            throw new AclsCommsException("Empty request message", ex);
        }
        expect(scanner, AbstractMessage.COMMAND_DELIMITER);
        try {
            RequestType type = RequestType.parse(command);
            switch (type) {
            case LOGIN: 
            case VIRTUAL_LOGIN:
            case NEW_VIRTUAL_LOGIN:
            case STAFF_LOGIN:
                return readLoginRequest(scanner, type);
            case LOGOUT: 
                return readLogoutRequest(scanner, type);
            case VIRTUAL_LOGOUT:
                return readVirtualLogoutRequest(scanner, type);
            case ACCOUNT: 
            case VIRTUAL_ACCOUNT:
            case NEW_VIRTUAL_ACCOUNT:
                return readAccountRequest(scanner, type);
            case NOTES:
                return readNotesRequest(scanner, type);
            case FACILITY_NAME:
            case FACILITY_COUNT:
            case FACILITY_LIST:
            case USE_PROJECT:
            case USE_TIMER:
            case USE_FULL_SCREEN:
            case USE_VIRTUAL:
            case SYSTEM_PASSWORD:
            case NET_DRIVE:
                return readQuery(scanner, type);
            default:
                throw new AssertionError("not implemented");
            }
        } catch (IllegalArgumentException ex) {
            throw new AclsMessageSyntaxException(ex.getMessage(), ex);
        }
    }

    private Request readQuery(Scanner scanner, RequestType type) 
            throws AclsException {
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new SimpleRequest(type, facility, clientAddr, localHostId);
    }

    private Request readNotesRequest(Scanner scanner, RequestType type) 
            throws AclsException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.NOTE_DELIMITER);
        String notes = nextNotes(scanner);
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new NoteRequest(userName, account, notes, 
                facility, clientAddr, localHostId);
    }

    private Request readLoginRequest(Scanner scanner, RequestType type) 
            throws AclsException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String password = nextPassword(scanner);
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new LoginRequest(type, userName, password,
                facility, clientAddr, localHostId);
    }

    private Request readVirtualLogoutRequest(Scanner scanner, RequestType type) 
            throws AclsException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String password = nextPassword(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new LogoutRequest(type, userName, password, account, 
                facility, clientAddr, localHostId);
    }

    private Request readLogoutRequest(Scanner scanner, RequestType type) 
            throws AclsException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new LogoutRequest(type, userName, null, account, 
                facility, clientAddr, localHostId);
    }
    
    private Request readAccountRequest(Scanner scanner, RequestType type) 
            throws AclsException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new AccountRequest(type, userName, account, 
                facility, clientAddr, localHostId);
    }
    
    private void determineFacility(Scanner scanner, RequestType type) 
            throws AclsMessageSyntaxException, ConfigurationException {
        facility = null;
        facilityName = null;
        localHostId = null;
        if (type.isVmfl()) {
            switch (type) {
            case USE_VIRTUAL:
            case FACILITY_LIST:
            case FACILITY_COUNT:
                break;
            default:
                expect(scanner, AbstractMessage.DELIMITER);
                expect(scanner, AbstractMessage.FACILITY_DELIMITER);
                facilityName = nextSubfacility(scanner);
            }
        } else {
            switch (type) {
            case FACILITY_NAME:
            case USE_PROJECT:
            case USE_TIMER:
            case USE_FULL_SCREEN:
            case SYSTEM_PASSWORD:
            case NET_DRIVE:
                if (scanner.hasNext()) {
                    localHostId = scanner.next().trim();
                }
                break;
            default:
                String token = scanner.next();
                if (token.equals(AbstractMessage.DELIMITER)) {
                    token = scanner.next();
                }
                if (token.equals(AbstractMessage.COMMAND_DELIMITER)) {
                    localHostId = nextLocalHostId(scanner);
                }
            }
        }
        localHostId = tidy(localHostId);
        facilityName = tidy(facilityName);
        facility = facilityMapper.lookup(localHostId, facilityName, clientAddr);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Mapped " + localHostId + "," + facilityName + "," + clientAddr + 
                    " to " + (facility == null ? "null" : 
                        ("facility " + facility.getFacilityName())));
        }
    }

    private String tidy(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            return str.isEmpty() ? null : str;
        }
    }
}
