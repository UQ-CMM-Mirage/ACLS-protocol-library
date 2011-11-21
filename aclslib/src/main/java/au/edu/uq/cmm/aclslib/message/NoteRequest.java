package au.edu.uq.cmm.aclslib.message;

/**
 * This class represents a Notes request.  It is not clear if this request 
 * makes any sense in the context of the virtual facility mechanism.
 * 
 * @author scrawley
 */
public class NoteRequest extends AbstractRequest {

    private String userName;
    private String account;
    private String notes;

    /**
     * Construct the message object
     * 
     * @param userName
     * @param account
     * @param notes the notes should be safe for transmission in a 
     *     message.  By convention, embedded newlines should replaced
     *     with semicolons.
     */
    public NoteRequest(String userName, String account, String notes) {
        super(RequestType.NOTES);
        this.userName = checkName(userName);
        this.account = checkAccount(account);
        this.notes = checkNotes(notes);
    }

    public String unparse() {
        return generateHeader() + userName + DELIMITER + 
                ACCOUNT_DELIMITER + account + DELIMITER + 
                FACILITY_DELIMITER + notes + DELIMITER;
    }

    /**
     * @return the current user's name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the current user's account name
     */
    public String getAccount() {
        return account;
    }

    /**
     * @return the notes.
     */
    public String getNotes() {
        return notes;
    }

}
