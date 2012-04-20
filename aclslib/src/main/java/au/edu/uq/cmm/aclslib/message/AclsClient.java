package au.edu.uq.cmm.aclslib.message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple low-level ACLS client class that handles a request / response
 * interaction between ourselves and an ACLS server (or proxy).
 * 
 * @author scrawley
 */
public class AclsClient {
    private static final Logger LOG = LoggerFactory.getLogger(AclsClient.class);
    private static final int ACLS_REQUEST_TIMEOUT = 5000;
    private final String serverHost;
    private final int serverPort;
    
    
    public AclsClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }
    
    public Response serverSendReceive(Request r) throws AclsException {
        try {
            Socket aclsSocket = new Socket();
            try {
                aclsSocket.setSoTimeout(ACLS_REQUEST_TIMEOUT);
                aclsSocket.connect(
                        new InetSocketAddress(serverHost, serverPort),
                        ACLS_REQUEST_TIMEOUT);
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        aclsSocket.getOutputStream()));
                InputStream is = aclsSocket.getInputStream();
                LOG.debug("Sending ACLS server request " + r.getType().name() +
                        "(" + r.unparse(true) + ")");
                w.append(r.unparse(false) + "\r\n").flush();
                return new ResponseReaderImpl().readWithStatusLine(is);
            } catch (ServerStatusException ex) {
                LOG.error("ACLS server (" + serverHost + ":" + serverPort +
                		") refused request: " + ex);
                // FIXME - I think this is wrong.
                return new ProxyErrorResponse("Proxy got status error from ACLS server");
            } finally {
                aclsSocket.close();
            }
        } catch (IOException ex) {
            LOG.warn("IO error while trying to talk to ACLS server (" +
                    serverHost + ":" + serverPort + ")", ex);
            return new ProxyErrorResponse("Proxy cannot talk to ACLS server");
        }
    }

    public boolean checkForVmflSupport() {
        LOG.debug("Checking for vMFL capability");
        try {
            Request request = new SimpleRequest(RequestType.USE_VIRTUAL, null, null, null);
            Response response = serverSendReceive(request);
            switch (response.getType()) {
            case USE_VIRTUAL:
                YesNoResponse uv = (YesNoResponse) response;
                LOG.info("The 'useVirtual' request returned " + uv.isYes());
                return uv.isYes();
            default:
                throw new AclsProtocolException(
                        "Unexpected response to USE_VIRTUAL request - " + 
                                response.getType());
            }
        } catch (AclsException ex) {
            // We do this in case we are talking to a server that is not
            // aware of the vMFL requests.
            LOG.info("The 'useVirtual' request failed - assuming no vMFL");
            LOG.debug("This is ths vMFL check failure cause", ex);
            return false;
        }
    }
}