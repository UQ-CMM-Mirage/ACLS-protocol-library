package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.net.Socket;
import java.util.Date;

import org.slf4j.Logger;

import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
import au.edu.uq.cmm.aclslib.authenticator.Authenticator;
import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.server.RequestProcessorBase;

public abstract class ProxyRequestProcessor extends RequestProcessorBase {
    private AclsClient client;
    private AclsProxy proxy;
    
    public ProxyRequestProcessor(
            ACLSProxyConfiguration config, FacilityMapper mapper,
            Logger logger, Socket socket, AclsProxy proxy) {
        super(config, mapper, logger, socket);
        this.proxy = proxy;
        this.client = new AclsClient(
                config.getServerHost(), config.getServerPort());
    }

    protected void doProcess(Request m, BufferedWriter w) 
            throws AclsException {
        // These methods will deal with the server interaction (if required)
        // and create and return the relevant response.
        getLogger().debug("Request is " + m.getType().name() + "(" + m.unparse(true) + ")");
        switch (m.getType()) {
        case LOGIN:
            processLoginRequest(m, w);
            break;
        case LOGOUT: 
            processLogoutRequest(m, w);
            break;
        case ACCOUNT:
            processAccountRequest(m, w);
            break;
        case NOTES:
            processNotesRequest(m, w);
            break;
        case FACILITY_NAME:
            processFacilityRequest(m, w);
            break;
        case USE_PROJECT: 
            processUseProjectRequest(m, w);
            break;
        case USE_TIMER: 
            processUseTimerRequest(m, w);
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
        case NET_DRIVE: 
            processNetDriveRequest(m, w);
            break;
        case USE_FULL_SCREEN:
            processUseFullScreenRequest(m, w);
            break;
        default:
            // We have told the client that we don't support the virtual
            // extensions, so it is an error for it to send them to us.
            // Anything else ... is an extension we don't understand.
            getLogger().error("Unexpected request type: " + m.getType());
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
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processNetDriveRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processStaffLoginRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processSystemPasswordRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processUseVirtualRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processUseTimerRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processUseProjectRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processFacilityRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processNotesRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processAccountRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processLogoutRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected abstract void processLoginRequest(
            Request m, BufferedWriter w) throws AclsException;

    protected Response tryFallbackAuthentication(LoginRequest l) throws AclsException {
        Response r;
        Authenticator fallbackAuthenticator = getProxy().getFallbackAuthenticator();
        AclsLoginDetails details = null;
        if (fallbackAuthenticator != null) {
            getLogger().debug("Attempting fallback authentication for " + 
                    l.getUserName() + " on " + l.getFacility().getFacilityName());
            details = fallbackAuthenticator.authenticate(l.getUserName(), l.getPassword(), l.getFacility());
            getLogger().debug("Fallback authentication " + 
                    (details == null ? "succeeded" : "failed"));
        }
        if (details != null) {
            r = new LoginResponse(ResponseType.LOGIN_ALLOWED, 
                    details.getUserName(), details.getOrgName(), new Date().toString(),
                    details.getAccounts(), details.getCertification(), details.isOnsiteAssist());
        } else {
            // I wish we could tell the user what really happened ...
            r = new RefusedResponse(ResponseType.LOGIN_REFUSED);
        }
        return r;
    }
}
