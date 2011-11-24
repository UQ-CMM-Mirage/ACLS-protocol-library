package au.edu.uq.cmm.aclslib.proxy;

import java.io.IOException;

public class ProxyException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5717816319649323095L;

    public ProxyException(String msg, IOException ex) {
        super(msg, ex);
    }

}
