package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.net.Socket;

import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AccountRequest;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.FacilityNameResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LogoutRequest;
import au.edu.uq.cmm.aclslib.message.NetDriveResponse;
import au.edu.uq.cmm.aclslib.message.NoteRequest;
import au.edu.uq.cmm.aclslib.message.ProxyErrorResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;

/**
 * @author scrawley
 */
public class NormalRequestProcessor extends ProxyRequestProcessor {
    
    public NormalRequestProcessor(
            ACLSProxyConfiguration config, FacilityMapper mapper,
            Socket socket, AclsProxy proxy) {
        super(config, mapper, LoggerFactory.getLogger(NormalRequestProcessor.class), socket, proxy);
    }

    protected void processUseFullScreenRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Uses a facility-specific configuration setting
        Response r = new YesNoResponse(m.getFacility().isUseFullScreen() ? 
                ResponseType.FULL_SCREEN_YES : ResponseType.FULL_SCREEN_NO);
        sendResponse(w, r);
    }

    protected void processNetDriveRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Uses facility-specific configuration settings
        Response r;
        FacilityConfig f = m.getFacility();
        if (f.isUseNetDrive()) {
            r = new NetDriveResponse(f.getDriveName(), f.getFolderName(),
                    f.getAccessName(), f.getAccessPassword());
        } else {
            r = new NetDriveResponse();
        }
        sendResponse(w, r);
    }

    protected void processStaffLoginRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Pass a 'staff' request as-is.
        Response r = getClient().serverSendReceive(m);
        switch (r.getType()) {
        case STAFF_LOGIN_ALLOWED:
        case STAFF_LOGIN_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            getLogger().error("Unexpected response for staff login: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    protected void processSystemPasswordRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Pass a 'system password' request as-is.
        Response r = getClient().serverSendReceive(m);
        switch (r.getType()) {
        case SYSTEM_PASSWORD_YES:
        case SYSTEM_PASSWORD_NO:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            getLogger().error("Unexpected response for system password: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    protected void processUseVirtualRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Uses a hard-wired response.  We don't support proxying of virtual facilities.
        Response r = new YesNoResponse(ResponseType.USE_VIRTUAL, false);
        sendResponse(w, r);
    }

    protected void processUseTimerRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Uses a facility-specific configuration setting
        Response r = new YesNoResponse(m.getFacility().isUseTimer() ? 
                ResponseType.TIMER_YES : ResponseType.TIMER_NO);
        sendResponse(w, r);
    }

    protected void processUseProjectRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Uses a general configuration setting
        Response r = new YesNoResponse(getConfig().isUseProject() ? 
                ResponseType.PROJECT_YES : ResponseType.PROJECT_NO);
        sendResponse(w, r);
    }

    protected void processFacilityRequest(Request m, BufferedWriter w) 
            throws AclsException {
        // Uses a facility-specific configuration setting
        Response r = new FacilityNameResponse(m.getFacility().getFacilityDescription());
        sendResponse(w, r);
    }

    protected void processNotesRequest(Request m, BufferedWriter w) 
            throws AclsException {
        NoteRequest nr = (NoteRequest) m;
        String notes = nr.getNotes();
        Request vnr = new NoteRequest(nr.getUserName(), nr.getAccount(), notes, 
                m.getFacility(), null, nr.getLocalHostId());
        Response r = getClient().serverSendReceive(vnr);
        switch (r.getType()) {
        case NOTES_ALLOWED:
        case NOTES_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            getLogger().error("Unexpected response for notes: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    protected void processAccountRequest(Request m, BufferedWriter w) 
            throws AclsException {
        AccountRequest a = (AccountRequest) m;
        Request vl = new AccountRequest(
                RequestType.ACCOUNT, a.getUserName(), a.getAccount(), 
                m.getFacility(), null, a.getLocalHostId());
        Response r = getClient().serverSendReceive(vl);
        switch (r.getType()) {
        case ACCOUNT_ALLOWED:
            getProxy().sendEvent(new AclsLoginEvent(m.getFacility(), a.getUserName(), a.getAccount()));
            break;
        case LOGOUT_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            getLogger().error("Unexpected response for account: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    protected void processLogoutRequest(Request m, BufferedWriter w) 
                    throws AclsException {
        LogoutRequest l = (LogoutRequest) m;
        Request vl = new LogoutRequest(RequestType.LOGOUT, 
                l.getUserName(), null, l.getAccount(), 
                m.getFacility(), null, l.getLocalHostId());
        Response r = getClient().serverSendReceive(vl);
        switch (r.getType()) {
        case LOGOUT_ALLOWED:
        case LOGOUT_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            getLogger().error("Unexpected response for logout: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        // (Issue a logout event, even if the logout request was refused.)
        getProxy().sendEvent(new AclsLogoutEvent(m.getFacility(), l.getUserName(), l.getAccount()));
        sendResponse(w, r);
    }

    protected void processLoginRequest(Request m, BufferedWriter w) 
            throws AclsException {
        LoginRequest l = (LoginRequest) m;
        Request vl = new LoginRequest(RequestType.LOGIN, 
                l.getUserName(), l.getPassword(), 
                m.getFacility(), null, l.getLocalHostId());
        Response r = getClient().serverSendReceive(vl);
        switch (r.getType()) {
        case LOGIN_ALLOWED:
        case LOGIN_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            getLogger().error("Unexpected response for login: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }
}
