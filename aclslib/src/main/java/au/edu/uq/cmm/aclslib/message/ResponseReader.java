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
     * @throws AclsException 
     */
    Response read(InputStream source) throws AclsException;
    
    /**
     * Read and parse status line followed by a response from the supplied input stream
     * 
     * @param source the input stream
     * @return the response message, if parsing was successful.
     * @throws AclsException 
     */
    Response readWithStatusLine(InputStream source) throws AclsException;
}
