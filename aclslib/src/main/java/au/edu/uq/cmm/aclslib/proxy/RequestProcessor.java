package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import au.edu.uq.cmm.aclslib.message.AccountRequest;
import au.edu.uq.cmm.aclslib.message.AccountResponse;
import au.edu.uq.cmm.aclslib.message.AllowedResponse;
import au.edu.uq.cmm.aclslib.message.FacilityNameResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.LogoutRequest;
import au.edu.uq.cmm.aclslib.message.NetDriveResponse;
import au.edu.uq.cmm.aclslib.message.NoteRequest;
import au.edu.uq.cmm.aclslib.message.ProxyErrorResponse;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseReaderImpl;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.ServerStatusException;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
import au.edu.uq.cmm.aclslib.server.Configuration;
import au.edu.uq.cmm.aclslib.server.Facility;
import au.edu.uq.cmm.aclslib.server.RequestProcessorBase;

/**
 * @author scrawley
 */
public class RequestProcessor extends RequestProcessorBase {
    
    private AclsProxy proxy;

    public RequestProcessor(Configuration config, Socket socket, AclsProxy proxy) {
        super(config, socket);
        this.proxy = proxy;
    }

    protected void doProcess(Facility f, Request m, BufferedWriter w) throws IOException {
        // These methods will deal with the server interaction (if required)
        // and create and return the relevant response.
        LOG.debug("Request is " + m.getType().name() + "(" + m.unparse() + ")");
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

    private void processUseFullScreenRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Uses a facility-specific configuration setting
        Response r = new YesNoResponse(f.isUseFullScreen() ? 
                ResponseType.FULL_SCREEN_YES : ResponseType.FULL_SCREEN_NO);
        sendResponse(w, r);
    }

    private void processNetDriveRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Uses facility-specific configuration settings
        Response r;
        if (f.isUseNetDrive()) {
            r = new NetDriveResponse(f.getDriveName(), f.getFolderName(),
                    f.getAccessName(), f.getAccessPassword());
        } else {
            r = new NetDriveResponse();
        }
        sendResponse(w, r);
    }

    private void processStaffLoginRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Pass a 'staff' request as-is.
        Response r = serverSendReceive(m);
        switch (r.getType()) {
        case STAFF_LOGIN_ALLOWED:
        case STAFF_LOGIN_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            LOG.error("Unexpected response for staff login: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    private void processSystemPasswordRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Pass a 'system password' request as-is.
        Response r = serverSendReceive(m);
        switch (r.getType()) {
        case SYSTEM_PASSWORD_YES:
        case SYSTEM_PASSWORD_NO:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            LOG.error("Unexpected response for system password: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    private void processUseVirtualRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Uses a hard-wired response.  We don't support proxying of virtual facilities.
        Response r = new YesNoResponse(ResponseType.USE_VIRTUAL, false);
        sendResponse(w, r);
    }

    private void processUseTimerRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Uses a facility-specific configuration setting
        Response r = new YesNoResponse(f.isUseTimer() ? 
                ResponseType.TIMER_YES : ResponseType.TIMER_NO);
        sendResponse(w, r);
    }

    private void processUseProjectRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Uses a general configuration setting
        Response r = new YesNoResponse(getConfig().isUseProject() ? 
                ResponseType.PROJECT_YES : ResponseType.PROJECT_NO);
        sendResponse(w, r);
    }

    private void processFacilityRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Uses a facility-specific configuration setting
        Response r = new FacilityNameResponse(f.getFacilityName());
        sendResponse(w, r);
    }

    private void processNotesRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Modify a 'notes' request by adding the facility name to the notes text.
        NoteRequest nr = (NoteRequest) m;
        String notes = nr.getNotes();
        Request vnr = new NoteRequest(nr.getUserName(), nr.getAccount(), 
                f.getFacilityName() + ": " + notes);
        Response r = serverSendReceive(vnr);
        switch (r.getType()) {
        case NOTES_ALLOWED:
        case NOTES_REFUSED:
        case COMMAND_ERROR:
        case NO_RESPONSE:
            break;
        default:
            LOG.error("Unexpected response for notes: " + r.getType());
            r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
        }
        sendResponse(w, r);
    }

    private void processAccountRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Turn an 'account' request into a 'virtual_account' request, and 
        // map the response to the appropriate 'logout' response.
        AccountRequest a = (AccountRequest) m;
        Request vl = new AccountRequest(RequestType.VIRTUAL_ACCOUNT, 
                a.getUserName(), a.getAccount(), f.getFacilityId());
        {
            Response r;
            Response vr = serverSendReceive(vl);
            switch (vr.getType()) {
            case VIRTUAL_ACCOUNT_ALLOWED:
                proxy.sendEvent(new AclsLoginEvent(f, a.getUserName(), a.getAccount()));
                r = new AccountResponse(ResponseType.ACCOUNT_ALLOWED,
                        ((AccountResponse) vr).getLoginTimestamp());
                break;
            case VIRTUAL_LOGOUT_REFUSED:
                r = new RefusedResponse(ResponseType.ACCOUNT_REFUSED);
                break;
            case COMMAND_ERROR:
            case NO_RESPONSE:
                r = vr;
                break;
            default:
                LOG.error("Unexpected response for virtual account: " + vr.getType());
                r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
            }
            sendResponse(w, r);
        }
    }

    private void processLogoutRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Turn a 'logout' request into a 'virtual_logout' request, and 
        // map the response to the appropriate 'logout' response.
        LogoutRequest l = (LogoutRequest) m;
        Request vl = new LogoutRequest(RequestType.VIRTUAL_LOGOUT, 
                l.getUserName(), l.getAccount(), f.getFacilityId());
        {
            Response r;
            Response vr = serverSendReceive(vl);
            switch (vr.getType()) {
            case VIRTUAL_LOGOUT_ALLOWED:
                proxy.sendEvent(new AclsLogoutEvent(f, l.getUserName(), l.getAccount()));
                r = new AllowedResponse(ResponseType.LOGOUT_ALLOWED);
                break;
            case VIRTUAL_LOGOUT_REFUSED:
                r = new RefusedResponse(ResponseType.LOGOUT_REFUSED);
                break;
            case COMMAND_ERROR:
            case NO_RESPONSE:
                r = vr;
                break;
            default:
                LOG.error("Unexpected response for virtual logout: " + vr.getType());
                r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
            }
            sendResponse(w, r);
        }
    }

    private void processLoginRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        // Turn a 'login' request into a 'virtual_login' request, and 
        // map the response to the appropriate 'login' response.
        LoginRequest l = (LoginRequest) m;
        Request vl = new LoginRequest(RequestType.VIRTUAL_LOGIN, 
                l.getUserName(), l.getPassword(), f.getFacilityId());
        {
            Response r;
            Response vr = serverSendReceive(vl);
            switch (vr.getType()) {
            case VIRTUAL_LOGIN_ALLOWED:
                LoginResponse vlr = (LoginResponse) vr;
                r = new LoginResponse(ResponseType.LOGIN_ALLOWED, 
                        vlr.getUserName(), vlr.getOrgName(), 
                        vlr.getAccounts(), vlr.getCertification(), vlr.isOnsiteAssist());
                break;
            case VIRTUAL_LOGIN_REFUSED:
                r = new RefusedResponse(ResponseType.LOGIN_REFUSED);
                break;
            case COMMAND_ERROR:
            case NO_RESPONSE:
                r = vr;
                break;
            default:
                LOG.error("Unexpected response for virtual login: " + vr.getType());
                r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
            }
            sendResponse(w, r);
        }
    }
    
    private Response serverSendReceive(Request request) {
        return serverSendReceive(request, getConfig());
    }

    static Response serverSendReceive(Request request, Configuration config) {
        try {
            Socket aclsSocket = new Socket(
                    config.getServerHost(), config.getServerPort());
            try {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        aclsSocket.getOutputStream()));
                InputStream is = aclsSocket.getInputStream();
                sendRequest(w, request);
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
