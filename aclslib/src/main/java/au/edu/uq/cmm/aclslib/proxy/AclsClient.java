package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.message.ProxyErrorResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseReaderImpl;
import au.edu.uq.cmm.aclslib.message.ServerStatusException;
import au.edu.uq.cmm.aclslib.server.Configuration;

public class AclsClient {
    private static final Logger LOG = Logger.getLogger(AclsClient.class);
    private final Configuration config;
    
    public AclsClient(Configuration config) {
        this.config = config;
    }
    
    public Response serverSendReceive(Request r) {
        try {
            Socket aclsSocket = new Socket(
                    config.getServerHost(), config.getServerPort());
            try {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        aclsSocket.getOutputStream()));
                InputStream is = aclsSocket.getInputStream();
                LOG.debug("Sending ACLS server request " + r.getType().name() + "(" + r.unparse() + ")");
                w.append(r.unparse() + "\r\n").flush();
                return new ResponseReaderImpl().readWithStatusLine(is);
            } catch (ServerStatusException ex) {
                LOG.error("ACLS server refused request: " + ex);
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