package au.edu.uq.cmm.aclslib.message;

/**
 * This enum represents the ACLS response types, and handles the mapping
 * between the types and their respective code / command numbers.  Refer to
 * the ACLS protocol document for the details of what the various types mean.
 * 
 * @author scrawley
 */
public enum ResponseType {
    NO_RESPONSE(-1, false), 
    COMMAND_ERROR(0, false), 
    
    LOGIN_ALLOWED(11, false), LOGIN_REFUSED(12, false), 
    LOGOUT_ALLOWED(21, false), LOGOUT_REFUSED(22, false),
    ACCOUNT_ALLOWED(31, false), ACCOUNT_REFUSED(32, false),
    NOTES_ALLOWED(41, false), NOTES_REFUSED(42, false),
    FACILITY_ALLOWED(51, false), FACILITY_REFUSED(52, false),
    PROJECT_YES(61, false), PROJECT_NO(62, false),
    TIMER_YES(71, false), TIMER_NO(72, false),
    
    USE_VIRTUAL(81, true), 
    
    FACILITY_COUNT(91, true), FACILITY_LIST(101, true),
    VIRTUAL_LOGIN_ALLOWED(111, true), VIRTUAL_LOGIN_REFUSED(112, true), 
    VIRTUAL_LOGOUT_ALLOWED(121, true), VIRTUAL_LOGOUT_REFUSED(122, true),
    VIRTUAL_ACCOUNT_ALLOWED(131, true), VIRTUAL_ACCOUNT_REFUSED(132, true),
    NEW_VIRTUAL_LOGIN_ALLOWED(141, true), NEW_VIRTUAL_LOGIN_REFUSED(142, true), 
    NEW_VIRTUAL_ACCOUNT_ALLOWED(151, true), NEW_VIRTUAL_ACCOUNT_REFUSED(152, true),
    SYSTEM_PASSWORD_YES(201, false), SYSTEM_PASSWORD_NO(202, false),
    STAFF_LOGIN_ALLOWED(211, false), STAFF_LOGIN_REFUSED(212, false), 
    NET_DRIVE_YES(221, false), NET_DRIVE_NO(222, false),
    FULL_SCREEN_YES(231, false), FULL_SCREEN_NO(232, false);
    
    private final int code;
    private final boolean vmfl;
    
    ResponseType(int code, boolean vmfl) {
        this.code = code;
        this.vmfl = vmfl;
    }
    
    public int getCode() {
        return code;
    }
    
    public boolean isVmfl() {
        return vmfl;
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
