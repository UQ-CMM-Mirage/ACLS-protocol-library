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
     * @throws AclsProtocolException if there is a problem with the request; e.g. a 
     * message syntax error.
     */
    Request read(InputStream source) throws AclsProtocolException;
}
