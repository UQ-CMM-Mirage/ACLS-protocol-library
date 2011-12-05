package au.edu.uq.cmm.aclslib.message;

import java.io.InputStream;
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

    public Request read(InputStream source) {
        Scanner scanner = createLineScanner(source);
        String command;
        try {
            command = scanner.next();
        } catch (NoSuchElementException ex) {
            return null;
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
            case VIRTUAL_LOGOUT:
                return readLogoutRequest(scanner, type);
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

    private Request readQuery(Scanner scanner, RequestType type) {
        expectEnd(scanner);
        return new SimpleRequest(type);
    }

    private Request readNotesRequest(Scanner scanner) {
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

    private Request readLoginRequest(Scanner scanner, RequestType type) {
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

    private Request readLogoutRequest(Scanner scanner, RequestType type) {
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
        return new LogoutRequest(type, userName, account, facility);
    }

    private Request readAccountRequest(Scanner scanner, RequestType type) {
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
