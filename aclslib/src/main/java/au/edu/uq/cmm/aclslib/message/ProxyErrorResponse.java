package au.edu.uq.cmm.aclslib.message;

public class ProxyErrorResponse extends AbstractResponse {
    
    private String message;

    public ProxyErrorResponse(String message) {
        super(null);
        this.message = message;
    }

    public String unparse() {
        throw new UnsupportedOperationException("Don't unparse me!");
    }

    public String getMessage() {
        return message;
    }
}
