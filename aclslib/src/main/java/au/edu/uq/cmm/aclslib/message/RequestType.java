package au.edu.uq.cmm.aclslib.message;

/**
 * This enum represents the ACLS request types, and handles the mapping
 * between the types and their respective code / command numbers.  Refer to
 * the ACLS protocol document for the details of what the various types mean.
 * 
 * @author scrawley
 */
public enum RequestType {
    LOGIN(1, false, true), 
    LOGOUT(2, false, true), 
    ACCOUNT(3, false, true), 
    NOTES(4, false, true), 
    FACILITY_NAME(5, false, true), 
    USE_PROJECT(6, false),
    USE_TIMER(7, false), 
    USE_VIRTUAL(8, true),
    FACILITY_COUNT(9, true),
    FACILITY_LIST(10, true),
    VIRTUAL_LOGIN(11, true),
    VIRTUAL_LOGOUT(12, true),
    VIRTUAL_ACCOUNT(13, true),
    NEW_VIRTUAL_LOGIN(14, true), 
    NEW_VIRTUAL_ACCOUNT(15, true), 
    SYSTEM_PASSWORD(20, false), 
    STAFF_LOGIN(21, false), 
    NET_DRIVE(22, false), 
    USE_FULL_SCREEN(23, false);
    
    private final int code;
    private final boolean vmfl;
    private final boolean localHostIdAllowed;
    
    RequestType(int code, boolean vmfl, boolean localHostIdAllowed) {
        this.code = code;
        this.vmfl = vmfl;
        this.localHostIdAllowed = localHostIdAllowed;
    }
    
    RequestType(int code, boolean vmfl) {
        this(code, vmfl, false);
    }
    
    public int getCode() {
        return code;
    }
    
    public boolean isVmfl() {
        return vmfl;
    }
    
    public boolean isLocalHostIdAllowed() {
        return localHostIdAllowed;
    }

    public static RequestType parse(String str) {
        try {
            int code = Integer.parseInt(str);
            for (RequestType val : values()) {
                if (val.getCode() == code) {
                    return val;
                }
            }
            throw new IllegalArgumentException("Unknown request type ('" + str + "')");
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Request type not a number ('" + str + "')");
        }
    }
    
    public String toString() {
        return "" + code;
    }
}
