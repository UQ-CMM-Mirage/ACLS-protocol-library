package au.edu.uq.cmm.aclslib.server;

import java.io.IOException;

public class ServerException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5717816319649323095L;

    public ServerException(String msg, IOException ex) {
        super(msg, ex);
    }

    public ServerException(String msg) {
        super(msg);
    }
}
