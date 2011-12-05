package au.edu.uq.cmm.aclslib.message;

/**
 * This is the base class for ACLS request and response methods.
 * 
 * @author scrawley
 */
public abstract class AbstractMessage implements Message {
    
    // These special characters are used in the ACLS protocol.
    // Strictly speaking, many of them are markers rather than
    // delimiters, but I'm sticking with the ACLS terminology.
    
    public static final String YES = "YES";
    public static final String NO = "NO";
    public static final String VMFL = "vMFL";
    
    public static String DELIMITER = "|";
    public static String COMMAND_DELIMITER = ":";
    public static String ACCOUNT_DELIMITER = "]";
    public static String ACCOUNT_SEPARATOR = ";";
    public static String FACILITY_DELIMITER = "?";
    public static String CERTIFICATE_DELIMITER = "&";
    public static String ONSITE_ASSIST_DELIMITER = "~";
    public static String NOTE_DELIMITER = "~";
    public static String SYSTEM_PASSWORD_DELIMITER = "/";
    public static String TIME_DELIMITER = "[";
    public static String ALL_DELIMITERS = "|:];?&~/[";
    
    public static String ACCEPTED_IP_TAG = "IP Accepted";

    
    protected String checkName(String name) {
        return check(name, "Name", COMMAND_DELIMITER + DELIMITER);
    }

    protected String checkPassword(String name) {
        return check(name, "Password", COMMAND_DELIMITER + DELIMITER);
    }

    protected String checkFacility(String facility) {
        if (facility == null) {
            return null;
        }
        return check(facility, "Facility name", ACCOUNT_DELIMITER + DELIMITER);
    }

    protected String checkAccount(String account) {
        return check(account, "Account name", CERTIFICATE_DELIMITER + 
                ACCOUNT_DELIMITER + ACCOUNT_SEPARATOR + DELIMITER);
    }
    
    protected String checkOrganization(String org) {
        return check(org, "Organization name", FACILITY_DELIMITER + DELIMITER);
    }
    
    protected String checkNotes(String note) {
        return check(note, "Notes text", NOTE_DELIMITER + DELIMITER);
    }
    
    protected String checkSystemPassword(String password) {
        return check(password, "System password", 
                SYSTEM_PASSWORD_DELIMITER + DELIMITER);
    }
    
    protected String checkDrive(String drive) {
        return check(drive, "Drive name", ACCOUNT_DELIMITER);
    }
    
    protected String checkFolderName(String folder) {
        return check(folder, "Folder name", TIME_DELIMITER);
    }
    
    protected String checkAccessName(String access) {
        return check(access, "Access name", ONSITE_ASSIST_DELIMITER);
    }
    
    protected String checkAccessPassword(String password) {
        return check(password, "Access password", DELIMITER);
    }
    
    protected String checkDateTime(String dateTime) {
        if (!dateTime.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException(
                    "Malformed datestamp '" + dateTime + "'");
        }
        return dateTime;
    }
    
    private String check(String str, String desc, String forbidden) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (forbidden.indexOf(ch) >= 0 || Character.isISOControl(ch)) {
                throw new IllegalArgumentException(
                        desc + " '" + str + "' contains an illegal character");
            }
        }
        return str;
    }
    
}
