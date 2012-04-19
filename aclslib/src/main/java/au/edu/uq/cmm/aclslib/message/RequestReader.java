package au.edu.uq.cmm.aclslib.message;

import java.io.InputStream;

/**
 * This is the API for an ACLS request reader.
 * 
 * @author scrawley
 */
public interface RequestReader {

    /**
     * Read and parse a request from the supplied input stream
     * 
     * @param source the input stream
     * @return the request message, if parsing was successful.
     */
    Request read(InputStream source) throws AclsException;
}
