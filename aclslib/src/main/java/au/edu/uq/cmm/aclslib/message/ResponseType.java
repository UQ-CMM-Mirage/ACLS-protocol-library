package au.edu.uq.cmm.aclslib.message;

/**
 * This enum represents the ACLS response types, and handles the mapping
 * between the types and their respective code / command numbers.  Refer to
 * the ACLS protocol document for the details of what the various types mean.
 * 
 * @author scrawley
 */
public enum ResponseType {
    ERROR(0), 
    LOGIN_ALLOWED(11), LOGIN_REFUSED(12), 
    LOGOUT_ALLOWED(21), LOGOUT_REFUSED(22),
    ACCOUNT_ALLOWED(31), ACCOUNT_REFUSED(32),
    NOTES_ALLOWED(41), NOTES_REFUSED(42),
    FACILITY_ALLOWED(51), FACILITY_REFUSED(52),
    PROJECT_YES(61), PROJECT_NO(62),
    TIMER_YES(71), TIMER_NO(72),
    USE_VIRTUAL(81), FACILITY_COUNT(91), FACILITY_LIST(101),
    VIRTUAL_LOGIN_ALLOWED(111), VIRTUAL_LOGIN_REFUSED(112), 
    VIRTUAL_LOGOUT_ALLOWED(121), VIRTUAL_LOGOUT_REFUSED(122),
    VIRTUAL_ACCOUNT_ALLOWED(131), VIRTUAL_ACCOUNT_REFUSED(132),
    NEW_VIRTUAL_LOGIN_ALLOWED(141), NEW_VIRTUAL_LOGIN_REFUSED(142), 
    NEW_VIRTUAL_ACCOUNT_ALLOWED(151), NEW_VIRTUAL_ACCOUNT_REFUSED(152),
    SYSTEM_PASSWORD_YES(201), SYSTEM_PASSWORD_NO(202),
    STAFF_LOGIN_ALLOWED(211), STAFF_LOGIN_REFUSED(212), 
    NET_DRIVE_YES(221), NET_DRIVE_NO(222),
    FULLSCREEN_YES(231), FULLSCREEN_NO(232);
    
    private final int code;
    
    ResponseType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static ResponseType parse(String str) {
        try {
            int code = Integer.parseInt(str);
            for (ResponseType val : values()) {
                if (val.getCode() == code) {
                    return val;
                }
            }
            throw new IllegalArgumentException("Unknown response type ('" + str + "')");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Response type not a number ('" + str + "')");
        }
    }
    
    public String toString() {
        return "" + code;
    }
}
