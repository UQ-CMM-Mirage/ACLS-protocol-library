package au.edu.uq.cmm.aclslib.message;

/**
 * This enum represents the user's training certification.  The three states are
 * self explanatory.
 * 
 * @author scrawley
 */
public enum Certification {
    VALID("Valid Certificate"), 
    EXPIRED("Expired Certificate"), 
    NONE("No Certificate");
    
    private final String text;

    private Certification(String text) {
        this.text = text;
    }
    
    /**
     * Turn a certificate string from an ACLS message into a Certification value.
     * @param str the ACLS string
     * @return the corresponding Certification
     */
    public static Certification parse(String str) {
        for (Certification value : values()) {
            if (value.text.equalsIgnoreCase(str)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown certification ('" + str + "')");
    }

    @Override
    public String toString() {
        return text;
    }
}
