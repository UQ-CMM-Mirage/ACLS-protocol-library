package au.edu.uq.cmm.aclslib.message;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * This class is an ACLS response reader for versions 20.x and 30.x of the 
 * ACLS protocol.
 * 
 * @author scrawley
 */
public class ResponseReaderImpl extends AbstractReader implements ResponseReader {

    public Response read(InputStream source) {
        return readResponse(createScanner(source));
    }
    
    public Response readWithStatusLine(InputStream source) {
        Scanner scanner = createScanner(source);
        String statusLine = scanner.nextLine();
        if (!statusLine.equals(AbstractMessage.ACCEPTED_IP_TAG)) {
            throw new ServerStatusException(statusLine);
        }
        return readResponse(scanner);
    }
    
    private Response readResponse(Scanner scanner) {
        String command = scanner.next();
        expect(scanner, AbstractMessage.COMMAND_DELIMITER);
        try {
            ResponseType type = ResponseType.parse(command);
            switch (type) {
            case COMMAND_ERROR:
                return readCommandError(scanner);
            case LOGIN_ALLOWED: 
            case VIRTUAL_LOGIN_ALLOWED: 
            case NEW_VIRTUAL_LOGIN_ALLOWED: 
                return readLoginResponse(scanner, type);
            case ACCOUNT_ALLOWED:
            case VIRTUAL_ACCOUNT_ALLOWED:
            case NEW_VIRTUAL_ACCOUNT_ALLOWED:
                return readAccountResponse(scanner, type);
            case LOGIN_REFUSED: 
            case VIRTUAL_LOGIN_REFUSED:
            case NEW_VIRTUAL_LOGIN_REFUSED:
            case LOGOUT_REFUSED:
            case VIRTUAL_LOGOUT_REFUSED:
            case ACCOUNT_REFUSED:
            case VIRTUAL_ACCOUNT_REFUSED:
            case NEW_VIRTUAL_ACCOUNT_REFUSED:
            case NOTES_REFUSED:
            case FACILITY_REFUSED:
            case STAFF_LOGIN_REFUSED:
                return readRefused(scanner, type);
            case FACILITY_ALLOWED:
                return readFacility(scanner);
            case LOGOUT_ALLOWED:
            case VIRTUAL_LOGOUT_ALLOWED:
            case NOTES_ALLOWED:
            case STAFF_LOGIN_ALLOWED:
                return readAllowed(scanner, type);
            case PROJECT_YES:
            case TIMER_YES:
            case FULL_SCREEN_YES:
                return readYesNo(scanner, type, true);
            case PROJECT_NO:
            case TIMER_NO:
            case FULL_SCREEN_NO:
                return readYesNo(scanner, type, false);
            case USE_VIRTUAL:
                return readFacilityType(scanner);
            case FACILITY_COUNT:
                return readFacilityCount(scanner);
            case FACILITY_LIST:
                return readFacilityList(scanner);
            case SYSTEM_PASSWORD_NO:
            case SYSTEM_PASSWORD_YES:
                return readSystemPassword(scanner, type);
            case NET_DRIVE_NO:
            case NET_DRIVE_YES:
                return readNetDrive(scanner, type);
            default:
                throw new AssertionError("not implemented");
            }
        } catch (IllegalArgumentException ex) {
            throw new MessageSyntaxException(ex.getMessage(), ex);
        }
    }

    private Response readNetDrive(Scanner scanner, ResponseType type) {
        if (type == ResponseType.NET_DRIVE_NO) {
            return new NetDriveResponse();
        }
        if (scanner.findInLine("([^\\]]*)\\]([^\\[]*)\\[([^~]*)~([^|]*)") == null) {
            throw new MessageSyntaxException("Cannot decode 'NetDrive' response");
        }
        MatchResult result = scanner.match();
        String driveName = result.group(1);
        String folderName = result.group(2);
        String accessName = result.group(3);
        String accessPassword = result.group(4);
        expectEnd(scanner);
        return new NetDriveResponse(driveName, folderName, accessName, accessPassword);
    }

    private Response readSystemPassword(Scanner scanner, ResponseType type) {
        String password = null;
        if (type == ResponseType.SYSTEM_PASSWORD_YES) {
            expect(scanner, AbstractMessage.SYSTEM_PASSWORD_DELIMITER);
            password = nextSystemPassword(scanner);
            if (password.equals(AbstractMessage.DELIMITER)) {
                password = "";
            } 
            expectEnd(scanner);
        }
        return new SystemPasswordResponse(password);
    }

    private Response readFacilityType(Scanner scanner) {
        expect(scanner, AbstractMessage.FACILITY_DELIMITER);
        String valueString = scanner.next();
        boolean value;
        if (valueString.equalsIgnoreCase(AbstractMessage.VMFL)) {
            value = true;
        } else if (valueString.equalsIgnoreCase(AbstractMessage.NO)) {
            value = false;
        } else {
            throw new MessageSyntaxException(
                    "Expected 'Yes' or 'No' but got '" + valueString + "'");
        }
        expectEnd(scanner);
        return new YesNoResponse(ResponseType.USE_VIRTUAL, value);
    }

    private Response readFacilityCount(Scanner scanner) {
        expect(scanner, AbstractMessage.FACILITY_DELIMITER);
        String countString = scanner.next();
        int count;
        try {
            count = Integer.parseInt(countString);
        } catch (NumberFormatException ex) {
            throw new MessageSyntaxException(
                    "Invalid facility count '" + countString + "'");
        }
        expectEnd(scanner);
        return new FacilityCountResponse(count);
    }

    private Response readYesNo(Scanner scanner, ResponseType type, boolean b) {
        expectEnd(scanner);
        return new YesNoResponse(type, b);
    }

    private Response readCommandError(Scanner scanner) {
        expectEnd(scanner);
        return new CommandErrorResponse();
    }

    private Response readRefused(Scanner scanner, ResponseType type) {
        expectEnd(scanner);
        return new RefusedResponse(type);
    }

    private Response readAllowed(Scanner scanner, ResponseType type) {
        expectEnd(scanner);
        return new AllowedResponse(type);
    }

    private Response readFacility(Scanner scanner) {
        expect(scanner, AbstractMessage.FACILITY_DELIMITER);
        String facility = nextFacility(scanner);
        expectEnd(scanner);
        return new FacilityNameResponse(facility);
    }

    private Response readFacilityList(Scanner scanner) {
        expect(scanner, AbstractMessage.FACILITY_DELIMITER);
        List<String> list = new ArrayList<String>();
        String token = nextSubfacility(scanner);
        while (!token.equals(AbstractMessage.DELIMITER)) {
            list.add(token);
            expect(scanner, AbstractMessage.ACCOUNT_SEPARATOR);
            token = scanner.next();
        }
        expectEnd(scanner);
        return new FacilityListResponse(list);
    }

    private Response readLoginResponse(Scanner scanner, ResponseType type) {
        String userName = nextName(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        String orgName = nextOrganization(scanner);
        expect(scanner, AbstractMessage.DELIMITER);
        expect(scanner, AbstractMessage.ACCOUNT_DELIMITER);
        List<String> accounts = new ArrayList<String>();
        String token = nextAccount(scanner);
        while (!token.equals(AbstractMessage.DELIMITER)) {
            accounts.add(token);
            expect(scanner, AbstractMessage.ACCOUNT_SEPARATOR);
            token = scanner.next();
        }
        expect(scanner, AbstractMessage.CERTIFICATE_DELIMITER);
        Certification certification = Certification.parse(scanner.next());
        token = scanner.next();
        boolean onsiteAssist = false;
        if (token.equals(AbstractMessage.ONSITE_ASSIST_DELIMITER)) {
            onsiteAssist = scanner.next().equalsIgnoreCase(AbstractMessage.YES);
        }
        expectEnd(scanner);
        return new LoginResponse(type, userName, orgName, 
                accounts, certification, onsiteAssist);
    }

    private Response readAccountResponse(Scanner scanner, ResponseType type) {
        expect(scanner, AbstractMessage.TIME_DELIMITER);
        String timestamp = nextTimestamp(scanner);
        expectEnd(scanner);
        return new AccountResponse(type, timestamp);
    }
}
