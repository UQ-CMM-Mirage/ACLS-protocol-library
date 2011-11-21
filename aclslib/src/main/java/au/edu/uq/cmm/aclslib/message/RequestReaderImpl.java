package au.edu.uq.cmm.aclslib.message;

import java.io.InputStream;
import java.util.Scanner;

/**
 * This class is an ACLS request reader for versions 20.x and 30.x of the 
 * ACLS protocol.
 * 
 * @author scrawley
 */
public class RequestReaderImpl extends AbstractReader implements RequestReader {

    public Request read(InputStream source) {
        Scanner scanner = createScanner(source);
        scanner.useDelimiter(DEFAULT_DELIMITERS);
        String command = scanner.next();
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
            case USE_FULLSCREEN:
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
        String account = nextAccount(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.NOTE_DELIMITER);
        String notes = nextNotes(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expectEnd(scanner);
        return new NoteRequest(userName, account, notes);
    }

    private Request readLoginRequest(Scanner scanner, RequestType type) {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String password = nextPassword(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String facility = null;
        if (type != RequestType.LOGIN && type != RequestType.STAFF_LOGIN) {
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
            expect(scanner, AbstractMessage.DELIMITER);
        }
        expectEnd(scanner);
        return new LoginRequest(type, userName, password, facility);
    }

    private Request readLogoutRequest(Scanner scanner, RequestType type) {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String account = nextAccount(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String facility = null;
        if (type == RequestType.VIRTUAL_LOGOUT) {
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
            expect(scanner, AbstractMessage.DELIMITER);
        }
        expectEnd(scanner);
        return new LogoutRequest(type, userName, account, facility);
    }

    private Request readAccountRequest(Scanner scanner, RequestType type) {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String account = nextAccount(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String facility = null;
        if (type != RequestType.ACCOUNT) {
            expect(scanner, AbstractMessage.FACILITY_DELIMITER);
            facility = nextSubfacility(scanner);
            expect(scanner, AbstractMessage.DELIMITER);
        }
        expectEnd(scanner);
        return new AccountRequest(type, userName, account, facility);
    }
}
