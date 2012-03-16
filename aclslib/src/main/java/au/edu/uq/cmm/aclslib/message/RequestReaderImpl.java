package au.edu.uq.cmm.aclslib.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.config.Configuration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;

/**
 * This class is an ACLS request reader for versions 20.x and 30.x of the 
 * ACLS protocol.
 * 
 * @author scrawley
 */
public class RequestReaderImpl extends AbstractReader implements RequestReader {
    private static final Logger LOG = Logger.getLogger(RequestReaderImpl.class);
    
    private Configuration config;
    private InetAddress clientAddr;
    private FacilityConfig facility;
    private String facilityName;
    private String localHostId;
    
    public RequestReaderImpl(Configuration config, InetAddress clientAddr) {
        super(LOG);
        this.config = config;
        this.clientAddr = clientAddr;
    }

    public Request read(InputStream source) throws AclsProtocolException {
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
            throw new MessageSyntaxException(ex.getMessage(), ex);
        }
    }

    private Request readQuery(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new SimpleRequest(type, facility, clientAddr, localHostId);
    }

    private Request readNotesRequest(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
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
            throws MessageSyntaxException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String password = nextPassword(scanner);
        determineFacility(scanner, type);
        expectEnd(scanner);
        return new LoginRequest(type, userName, password,
                facility, clientAddr, localHostId);
    }

    private Request readVirtualLogoutRequest(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
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
            throws MessageSyntaxException {
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
            throws MessageSyntaxException {
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
            throws MessageSyntaxException {
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
                facility = config.lookupFacilityByName(facilityName);
            }
        } else if (type.isLocalHostIdAllowed()) {
            String token = nextDelimiter(scanner);
            if (token.equals(AbstractMessage.DELIMITER)) {
                token = nextDelimiter(scanner);
                if (token.equals(AbstractMessage.COMMAND_DELIMITER)) {
                    localHostId = nextLocalHostId(scanner);
                    facility = config.lookupFacilityByLocalHostId(localHostId);
                }
            }
        }
        if (facility == null) {
            facility = config.lookupFacilityByAddress(clientAddr);
        }
    }
}
