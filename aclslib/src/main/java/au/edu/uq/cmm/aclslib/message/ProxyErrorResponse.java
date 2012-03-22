package au.edu.uq.cmm.aclslib.message;

public class ProxyErrorResponse extends AbstractResponse {
    
    private String message;

    public ProxyErrorResponse(String message) {
        super(ResponseType.NO_RESPONSE);
        this.message = message;
    }

    public String unparse(boolean obscurePasswords) {
        throw new UnsupportedOperationException("Don't unparse me!");
    }

    public String getMessage() {
        return message;
    }
}
