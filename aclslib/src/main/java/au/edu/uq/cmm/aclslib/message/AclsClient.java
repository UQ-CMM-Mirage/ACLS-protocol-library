package au.edu.uq.cmm.aclslib.message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * A simple low-level ACLS client class that handles a request / response
 * interaction between ourselves and an ACLS server (or proxy).
 * 
 * @author scrawley
 */
public class AclsClient {
    private static final Logger LOG = Logger.getLogger(AclsClient.class);
    private final String serverHost;
    private final int serverPort;
    
    public AclsClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }
    
    public Response serverSendReceive(Request r) throws AclsException {
        try {
            Socket aclsSocket = new Socket(serverHost, serverPort);
            try {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        aclsSocket.getOutputStream()));
                InputStream is = aclsSocket.getInputStream();
                LOG.debug("Sending ACLS server request " + r.getType().name() + "(" + r.unparse() + ")");
                w.append(r.unparse() + "\r\n").flush();
                return new ResponseReaderImpl().readWithStatusLine(is);
            } catch (ServerStatusException ex) {
                LOG.error("ACLS server refused request: " + ex);
                // FIXME - I think this is wrong.
                return new ProxyErrorResponse("Proxy got status error from ACLS server");
            } finally {
                aclsSocket.close();
            }
        } catch (IOException ex) {
            LOG.warn("IO error while trying to talk to ACLS server", ex);
            return new ProxyErrorResponse("Proxy cannot talk to ACLS server");
        }
    }
}