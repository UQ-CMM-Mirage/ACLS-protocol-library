package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.net.Socket;

import au.edu.uq.cmm.aclslib.config.Configuration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.server.RequestProcessorBase;

public abstract class ProxyRequestProcessor extends RequestProcessorBase {
    private AclsClient client;
    private AclsProxy proxy;
    
    public ProxyRequestProcessor(
            Configuration config, Socket socket, AclsProxy proxy) {
        super(config, socket);
        this.proxy = proxy;
        this.client = new AclsClient(
                config.getServerHost(), config.getServerPort());
    }

    protected void doProcess(FacilityConfig f, Request m, BufferedWriter w) 
            throws AclsException {
        // These methods will deal with the server interaction (if required)
        // and create and return the relevant response.
        LOG.debug("Request is " + m.getType().name() + "(" + m.unparse(true) + ")");
        switch (m.getType()) {
        case LOGIN:
            processLoginRequest(f, m, w);
            break;
        case LOGOUT: 
            processLogoutRequest(f, m, w);
            break;
        case ACCOUNT:
            processAccountRequest(f, m, w);
            break;
        case NOTES:
            processNotesRequest(f, m, w);
            break;
        case FACILITY_NAME:
            processFacilityRequest(f, m, w);
            break;
        case USE_PROJECT: 
            processUseProjectRequest(f, m, w);
            break;
        case USE_TIMER: 
            processUseTimerRequest(f, m, w);
            break;
        case USE_VIRTUAL: 
            processUseVirtualRequest(f, m, w);
            break;
        case SYSTEM_PASSWORD: 
            processSystemPasswordRequest(f, m, w);
            break;
        case STAFF_LOGIN:
            processStaffLoginRequest(f, m, w);
            break;
        case NET_DRIVE: 
            processNetDriveRequest(f, m, w);
            break;
        case USE_FULL_SCREEN:
            processUseFullScreenRequest(f, m, w);
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

    protected AclsClient getClient() {
        return client;
    }

    protected AclsProxy getProxy() {
        return proxy;
    }

    protected abstract void processUseFullScreenRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processNetDriveRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processStaffLoginRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processSystemPasswordRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processUseVirtualRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processUseTimerRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processUseProjectRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processFacilityRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processNotesRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processAccountRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processLogoutRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;

    protected abstract void processLoginRequest(
            FacilityConfig f, Request m, BufferedWriter w) throws AclsException;
}
