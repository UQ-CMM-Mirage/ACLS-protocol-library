package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.net.Socket;
import java.util.Date;

import org.slf4j.Logger;

import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
import au.edu.uq.cmm.aclslib.authenticator.Authenticator;
import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.FacilityNameResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.NetDriveResponse;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
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
                config.getServerHost(), config.getServerPort(), 
                proxy.getTimeout());
    }

    protected void doProcess(Request m, BufferedWriter w) 
            throws AclsException {
        // These methods will deal with the server interaction (if required)
        // and create and return the relevant response.
        getLogger().debug("doRequest: request is " + m.getType().name() + "(" + m.unparse(true) + ")");
        Response r;
        switch (m.getType()) {
        case LOGIN:
            r = processLoginRequest(m);
            break;
        case LOGOUT: 
            r = processLogoutRequest(m);
            break;
        case ACCOUNT:
            r = processAccountRequest(m);
            break;
        case NOTES:
            r = processNotesRequest(m);
            break;
        case FACILITY_NAME:
            r = processFacilityRequest(m);
            break;
        case USE_PROJECT: 
            r = processUseProjectRequest(m);
            break;
        case USE_TIMER: 
            r = processUseTimerRequest(m);
            break;
        case USE_VIRTUAL: 
            r = processUseVirtualRequest(m);
            break;
        case SYSTEM_PASSWORD: 
            r = processSystemPasswordRequest(m);
            break;
        case STAFF_LOGIN:
            r = processStaffLoginRequest(m);
            break;
        case NET_DRIVE: 
            r = processNetDriveRequest(m);
            break;
        case USE_FULL_SCREEN:
            r = processUseFullScreenRequest(m);
            break;
        default:
            // We have told the client that we don't support the virtual
            // extensions, so it is an error for it to send them to us.
            // Anything else ... is an extension we don't understand.
            getLogger().error("Unexpected request type: " + m.getType());
            sendErrorResponse(w);
            r = null;
            break;
        }
        if (r != null) {
            getLogger().debug("Response is " + r.getType().name() + "(" + r.unparse(true) + ")");
            sendResponse(w, r);
        }
    }

    protected AclsClient getClient() {
        return client;
    }

    protected AclsProxy getProxy() {
        return proxy;
    }

    protected final Response processUseFullScreenRequest(Request m) 
            throws AclsException {
        // Uses a facility-specific configuration setting
        return new YesNoResponse(
                m.getFacility() != null && m.getFacility().isUseFullScreen() ? 
                ResponseType.FULL_SCREEN_YES : ResponseType.FULL_SCREEN_NO);
    }

    protected final Response processNetDriveRequest(Request m) 
            throws AclsException {
        // Uses facility-specific configuration settings
        FacilityConfig f = m.getFacility();
        if (f != null && f.isUseNetDrive()) {
            return new NetDriveResponse(f.getDriveName(), f.getFolderName(),
                    f.getAccessName(), f.getAccessPassword());
        } else {
            return new NetDriveResponse();
        }
    }
    
    protected final Response processStaffLoginRequest(Request m) 
            throws AclsException {
        // Pass a 'staff' request as-is.
        Response r = getClient().serverSendReceive(m);
        switch (r.getType()) {
        case STAFF_LOGIN_ALLOWED:
        case STAFF_LOGIN_REFUSED:
        case COMMAND_ERROR:
            return r;
        default:
            throw new AclsProtocolException(
                    "Unexpected response for staff login: " + r.getType());
        }
    }

    protected final Response processSystemPasswordRequest(Request m) 
            throws AclsException {
        // Pass a 'system password' request as-is.
        Response r = getClient().serverSendReceive(m);
        switch (r.getType()) {
        case SYSTEM_PASSWORD_YES:
        case SYSTEM_PASSWORD_NO:
        case COMMAND_ERROR:
            return r;
        default:
            throw new AclsProtocolException(
                    "Unexpected response for system password: " + r.getType());
        }
    }
    
    protected final Response processUseVirtualRequest(Request m) 
            throws AclsException {
        // Uses a hard-wired response.  We do not support proxying of virtual facilities.
        return new YesNoResponse(ResponseType.USE_VIRTUAL, false);
    }

    protected final Response processUseTimerRequest(Request m) 
            throws AclsException {
        // Uses a facility-specific configuration setting
        return new YesNoResponse(
                m.getFacility() != null && m.getFacility().isUseTimer() ? 
                ResponseType.TIMER_YES : ResponseType.TIMER_NO);
    }

    protected final Response processUseProjectRequest(Request m) 
            throws AclsException {
        // Uses a general configuration setting
        return new YesNoResponse(getConfig().isUseProject() ? 
                ResponseType.PROJECT_YES : ResponseType.PROJECT_NO);
    }

    protected final Response processFacilityRequest(Request m) 
            throws AclsException {
        // Uses a facility-specific configuration setting
        return m.getFacility() != null ?
                new FacilityNameResponse(m.getFacility().getFacilityDescription()) :
                new RefusedResponse(ResponseType.FACILITY_REFUSED);
    }

    protected abstract Response processNotesRequest(
            Request m) throws AclsException;

    protected abstract Response processAccountRequest(
            Request m) throws AclsException;

    protected abstract Response processLogoutRequest(
            Request m) throws AclsException;

    protected abstract Response processLoginRequest(
            Request m) throws AclsException;

    protected Response tryFallbackAuthentication(LoginRequest l) throws AclsException {
        Response r;
        Authenticator fallbackAuthenticator = getProxy().getFallbackAuthenticator();
        AclsLoginDetails details = null;
        if (fallbackAuthenticator != null) {
            getLogger().debug("Attempting fallback authentication for " + 
                    l.getUserName() + " on " + l.getFacility().getFacilityName());
            details = fallbackAuthenticator.authenticate(l.getUserName(), l.getPassword(), l.getFacility());
            getLogger().debug("Fallback authentication " + 
                    (details != null ? "succeeded" : "failed"));
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
