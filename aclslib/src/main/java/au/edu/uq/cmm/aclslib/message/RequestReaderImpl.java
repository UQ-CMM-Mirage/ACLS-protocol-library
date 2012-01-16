package au.edu.uq.cmm.aclslib.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * This class is an ACLS request reader for versions 20.x and 30.x of the 
 * ACLS protocol.
 * 
 * @author scrawley
 */
public class RequestReaderImpl extends AbstractReader implements RequestReader {
    private static final Logger LOG = Logger.getLogger(RequestReaderImpl.class);
    
    public RequestReaderImpl() {
        super(LOG);
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
                return readNotesRequest(scanner);
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
        expectEnd(scanner);
        return new SimpleRequest(type);
    }

    private Request readNotesRequest(Scanner scanner) 
            throws MessageSyntaxException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.NOTE_DELIMITER);
        String notes = nextNotes(scanner);
        expectEnd(scanner);
        return new NoteRequest(userName, account, notes);
    }

    private Request readLoginRequest(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String password = nextPassword(scanner);
        String facility = null;
        if (type != RequestType.LOGIN && type != RequestType.STAFF_LOGIN) {
            expect(scanner, AbstractMessage.DELIMITER);
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
        }
        expectEnd(scanner);
        return new LoginRequest(type, userName, password, facility);
    }

    private Request readVirtualLogoutRequest(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String password = nextPassword(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        String facility = null;
        if (type == RequestType.VIRTUAL_LOGOUT) {
            expect(scanner, AbstractMessage.DELIMITER);
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
        }
        expectEnd(scanner);
        return new LogoutRequest(type, userName, password, account, facility);
    }

    private Request readLogoutRequest(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        String facility = null;
        if (type == RequestType.VIRTUAL_LOGOUT) {
            expect(scanner, AbstractMessage.DELIMITER);
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
        }
        expectEnd(scanner);
        return new LogoutRequest(type, userName, null, account, facility);
    }
    
    private Request readAccountRequest(Scanner scanner, RequestType type) 
            throws MessageSyntaxException {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        String account = nextAccount(scanner);
        String facility = null;
        if (type != RequestType.ACCOUNT) {
            expect(scanner, AbstractMessage.DELIMITER);
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
        }
        expectEnd(scanner);
        return new AccountRequest(type, userName, account, facility);
    }
}
