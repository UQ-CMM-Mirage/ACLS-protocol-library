package au.edu.uq.cmm.aclslib.message;

/**
 * This exception is thrown in response to an unexpected status line
 * in an ACLS response.
 * 
 * @author scrawley
 */
public class ServerStatusException extends AclsProtocolException {
    private static final long serialVersionUID = 7732308813287253138L;
    
    private String statusLine;

    public ServerStatusException(String statusLine) {
        super("Server rejected request: " + statusLine);
        this.statusLine = statusLine;
    }

    public String getStatusLine() {
        return statusLine;
    }

}
