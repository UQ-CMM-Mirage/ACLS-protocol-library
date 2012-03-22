package au.edu.uq.cmm.aclslib.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.config.Configuration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.message.AbstractMessage;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.CommandErrorResponse;
import au.edu.uq.cmm.aclslib.message.ProxyErrorResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestReader;
import au.edu.uq.cmm.aclslib.message.RequestReaderImpl;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.proxy.VmflRequestProcessor;

public abstract class RequestProcessorBase  implements Runnable {

    protected static final Logger LOG = Logger.getLogger(VmflRequestProcessor.class);
    
    private Socket socket;
    private Configuration config;

    public RequestProcessorBase(Configuration config, Socket socket) {
        super();
        this.socket = socket;
        this.config = config;
    }

    protected void sendErrorResponse(BufferedWriter w) throws AclsCommsException {
        sendResponse(w, new CommandErrorResponse());
    }

    protected void sendResponse(BufferedWriter w, Response r) 
        throws AclsCommsException {
        try {
        if (r instanceof ProxyErrorResponse) {
            LOG.error("Proxy error - " + ((ProxyErrorResponse) r).getMessage());
            w.append(new CommandErrorResponse().unparse(false) + "\r\n").flush();
        } else {
            LOG.debug("Sending response " + r.getType().name() + "(" + r.unparse(true) + ")");
            w.append(r.unparse(false) + "\r\n").flush();
        }
        } catch (IOException ex) {
            throw new AclsCommsException("Couldn't write message", ex);
        }
    }

    protected static void sendRequest(BufferedWriter w, Request r) 
            throws IOException {
        LOG.debug("Sending request " + r.getType().name() + "(" + r.unparse(true) + ")");
        w.append(r.unparse(false) + "\r\n").flush();
    }

    public void run() {
        try {
            InetAddress addr = socket.getInetAddress();
            LOG.debug("Processing request from " + addr);
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            // Figure out which 'facility' is talking to us, based on the client
            // IP address.  If the IP address is not known to us, we can't map it
            // to a virtual facility id to log the user in ... so the only sensible
            // thing to do is send a status error.
            FacilityConfig f = config.lookupFacilityByAddress(addr);
            if (f == null) {
                LOG.debug("Unknown facility: IP is " + addr);
                w.append("Proxy has no facility details for " + addr + "\r\n").flush();
                return;
            }
            w.append(AbstractMessage.ACCEPTED_IP_TAG + "\r\n").flush();
            
            // Now read the request ...
            InputStream is = socket.getInputStream();
            RequestReader reader = new RequestReaderImpl(config, socket.getInetAddress());
            Request m = reader.read(is);
            // ... and dispatch to a "process" method bases on the request type.
            // These methods will deal with the server interaction (if required)
            // and create and return the relevant response.
            LOG.debug("Request is " + m.getType().name() + "(" + m.unparse(true) + ")");
            doProcess(f, m, w);
        } catch (IOException ex) {
            LOG.error(ex);
        } catch (AclsException ex) {
            LOG.error(ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                // ignore this.
            }
        }
    }

    protected abstract void doProcess(FacilityConfig f, Request m, BufferedWriter w) 
            throws AclsException;

    public Configuration getConfig() {
        return config;
    }
}