package au.edu.uq.cmm.aclslib.message;

import java.io.InputStream;

/**
 * This is the API for an ACLS response reader.
 * 
 * @author scrawley
 */
public interface ResponseReader {

    /**
     * Read and parse a response from the supplied input stream
     * 
     * @param source the input stream
     * @return the response message, if parsing was successful.
     * @throws AclsProtocolException if there is a problem with the request; e.g. a 
     * message syntax error.
     */
    Response read(InputStream source) throws AclsProtocolException;
    
    /**
     * Read and parse status line followed by a response from the supplied input stream
     * 
     * @param source the input stream
     * @return the response message, if parsing was successful.
     * @throws AclsProtocolException if there is a problem with the request; e.g. a 
     * message syntax error or a status line other than the expected "IP Accepted"
     * status.
     */
    Response readWithStatusLine(InputStream source) throws AclsProtocolException;
}
