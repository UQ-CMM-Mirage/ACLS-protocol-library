package au.edu.uq.cmm.aclslib.message;

/**
 * This exception is thrown in response to an unexpected status line
 * in an ACLS response.
 * 
 * @author scrawley
 */
@SuppressWarnings("serial")
public class ServerStatusException extends AclsProtocolException {
    private String statusLine;

    public ServerStatusException(String statusLine) {
        super("Server rejected request: " + statusLine);
        this.statusLine = statusLine;
    }

    public String getStatusLine() {
        return statusLine;
    }

}
