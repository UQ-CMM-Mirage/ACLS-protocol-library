package au.edu.uq.cmm.aclslib.message;

/**
 * This enum represents the ACLS request types, and handles the mapping
 * between the types and their respective code / command numbers.  Refer to
 * the ACLS protocol document for the details of what the various types mean.
 * 
 * @author scrawley
 */
public enum RequestType {
    LOGIN(1), LOGOUT(2), ACCOUNT(3), NOTES(4), FACILITY_NAME(5), USE_PROJECT(6), 
    USE_TIMER(7), USE_VIRTUAL(8), FACILITY_COUNT(9), FACILITY_LIST(10),
    VIRTUAL_LOGIN(11), VIRTUAL_LOGOUT(12), VIRTUAL_ACCOUNT(13),
    NEW_VIRTUAL_LOGIN(14), NEW_VIRTUAL_ACCOUNT(15), 
    SYSTEM_PASSWORD(20), STAFF_LOGIN(21), NET_DRIVE(22), USE_FULLSCREEN(23);
    
    private final int code;
    
    RequestType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
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
