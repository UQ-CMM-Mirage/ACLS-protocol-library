package au.edu.uq.cmm.aclslib.dummy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.message.AllowedResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.SystemPasswordResponse;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
import au.edu.uq.cmm.aclslib.proxy.AclsProxy;
import au.edu.uq.cmm.aclslib.proxy.RequestProcessorBase;
import au.edu.uq.cmm.aclslib.server.Configuration;
import au.edu.uq.cmm.aclslib.server.Facility;

public class RequestProcessor extends RequestProcessorBase implements Runnable {
    private static final Logger LOG = Logger.getLogger(AclsProxy.class);
    
    public RequestProcessor(Configuration config, Socket socket) {
        super(config, socket);
    }

    protected void doProcess(Facility f, Request m, BufferedWriter w) {
        LOG.debug("Request is " + m.getType().name() + "(" + m.unparse() + ")");
        switch (m.getType()) {
        case VIRTUAL_LOGIN:
            processLoginRequest(m, w);
            break;
        case VIRTUAL_LOGOUT: 
            processLogoutRequest(m, w);
            break;
        case VIRTUAL_ACCOUNT:
            processAccountRequest(m, w);
            break;
        case NOTES:
            processNotesRequest(m, w);
            break;
        case USE_PROJECT: 
            processUseProjectRequest(m, w);
            break;
        case USE_VIRTUAL: 
            processUseVirtualRequest(m, w);
            break;
        case SYSTEM_PASSWORD: 
            processSystemPasswordRequest(m, w);
            break;
        case STAFF_LOGIN:
            processStaffLoginRequest(m, w);
            break;
        default:
            // We have told the client that we don't support the virtual
            // extensions, so it is an error for it to send them to us.
            // Anything else ... is an extension we don't understand.
            LOG.error("Unexpected request type: " + m.getType());
            sendErrorResponse(w);
            break;
        }
    }

    private void processUseVirtualRequest(Request m, BufferedWriter w) 
            throws IOException {
        Response r = new YesNoResponse(ResponseType.USE_VIRTUAL, true);
        sendResponse(w, r);
    }

    private void processStaffLoginRequest(Request m, BufferedWriter w) 
            throws IOException {
        LoginRequest lr = (LoginRequest) m;
        Response r;
        if (lr.getPassword().equals("secret")) {
            r = new AllowedResponse(ResponseType.STAFF_LOGIN_ALLOWED);
        } else {
            r = new RefusedResponse(ResponseType.STAFF_LOGIN_REFUSED);
        }
        sendResponse(w, r);
    }

    private void processSystemPasswordRequest(Request m, BufferedWriter w) 
            throws IOException {
        Response r = new SystemPasswordResponse("secret");
        sendResponse(w, r);
    }
    
    
}
