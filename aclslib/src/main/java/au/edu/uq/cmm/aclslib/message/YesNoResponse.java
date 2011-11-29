package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents one of a number of different kinds of yes/no
 * responses to requests.  There is some inconsistency in the way that
 * these responses are encoded, and we attempt to hide that.
 * 
 * @author scrawley
 */
public class YesNoResponse extends AbstractResponse {

    private boolean value;

    /**
     * Construct the request to contain a specific value / type
     * 
     * @param type
     * @param value
     */
    public YesNoResponse(ResponseType type, boolean value) {
        super(type);
        switch (getType()) {
        case PROJECT_NO:
        case FULL_SCREEN_NO:
        case TIMER_NO:
            if (value) {
                throw new AssertionError("Wrong value");
            }
            break;
        case FULL_SCREEN_YES:
        case PROJECT_YES:
        case TIMER_YES:
            if (!value) {
                throw new AssertionError("Wrong value");
            }
            break;
        case USE_VIRTUAL:
            break;
        default:
            throw new AssertionError("Unexpected type (" + getType() + ")");
        }
        this.value = value;
    }

    /**
     * Construct the request to with a value implied by the type
     * @param type
     */
    public YesNoResponse(ResponseType type) {
        super(type);
        switch (getType()) {
        case PROJECT_NO:
        case FULL_SCREEN_NO:
        case TIMER_NO:
            this.value = false;
            break;
        case FULL_SCREEN_YES:
        case PROJECT_YES:
        case TIMER_YES:
            this.value = true;
            break;
        default:
            throw new AssertionError("Unexpected type (" + getType() + ")");
        }
    }

    public String unparse() {
        switch (getType()) {
        case PROJECT_NO:
        case PROJECT_YES:
        case FULL_SCREEN_NO:
        case FULL_SCREEN_YES:
        case TIMER_NO:
        case TIMER_YES:
            return generateHeader();
        case USE_VIRTUAL:
            return generateHeader() + FACILITY_DELIMITER +
                    (value ? YES : NO) + DELIMITER;
        default:
            throw new AssertionError("Unexpected type (" + getType() + ")");
        }
    }

    /**
     * @return the value in the response.
     */
    public boolean isYes() {
        return value;
    }
}
