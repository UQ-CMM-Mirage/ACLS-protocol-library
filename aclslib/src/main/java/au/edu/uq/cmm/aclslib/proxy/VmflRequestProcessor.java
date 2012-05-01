package au.edu.uq.cmm.aclslib.proxy;

import java.net.Socket;

import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AccountRequest;
import au.edu.uq.cmm.aclslib.message.AccountResponse;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.AllowedResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.LogoutRequest;
import au.edu.uq.cmm.aclslib.message.NoteRequest;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;

/**
 * @author scrawley
 */
public class VmflRequestProcessor extends ProxyRequestProcessor {
    
    public VmflRequestProcessor(
            ACLSProxyConfiguration config, FacilityMapper mapper,
            Socket socket, AclsProxy proxy) {
        super(config, mapper, 
                LoggerFactory.getLogger(VmflRequestProcessor.class),
                socket, proxy);
    }

    protected Response processNotesRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            // Modify a 'notes' request by adding the facility name to the notes text.
            NoteRequest nr = (NoteRequest) m;
            String notes = nr.getNotes();
            FacilityConfig f = m.getFacility();
            Request vnr = new NoteRequest(nr.getUserName(), nr.getAccount(), 
                    f.getFacilityDescription() + ": " + notes, f, null, null);
            r = getClient().serverSendReceive(vnr);
            switch (r.getType()) {
            case NOTES_ALLOWED:
            case NOTES_REFUSED:
            case COMMAND_ERROR:
                break;
            default:
                throw new AclsProtocolException(
                        "Unexpected response for notes: " + r.getType());
            }
        } else {
            r = new RefusedResponse(ResponseType.NOTES_REFUSED);
        }
        return r;
    }

    protected Response processAccountRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            // Turn an 'account' request into a 'virtual_account' request, and 
            // map the response to the appropriate 'logout' response.
            AccountRequest a = (AccountRequest) m;
            FacilityConfig f = m.getFacility();
            Request vl = new AccountRequest(RequestType.VIRTUAL_ACCOUNT, 
                    a.getUserName(), a.getAccount(), f, null, null);
            Response vr = getClient().serverSendReceive(vl);
            switch (vr.getType()) {
            case VIRTUAL_ACCOUNT_ALLOWED:
                getProxy().sendEvent(
                        new AclsLoginEvent(f, a.getUserName(), a.getAccount()));
                r = new AccountResponse(ResponseType.ACCOUNT_ALLOWED,
                        ((AccountResponse) vr).getLoginTimestamp());
                break;
            case VIRTUAL_LOGOUT_REFUSED:
                r = new RefusedResponse(ResponseType.ACCOUNT_REFUSED);
                break;
            case COMMAND_ERROR:
                r = vr;
                break;
            default:
                throw new AclsProtocolException(
                        "Unexpected response for virtual account: " + vr.getType());
            }
        } else {
            r = new RefusedResponse(ResponseType.ACCOUNT_REFUSED);
        }
        return r;
    }

    protected Response processLogoutRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            // Turn a 'logout' request into a 'virtual_logout' request, and 
            // map the response to the appropriate 'logout' response.
            LogoutRequest l = (LogoutRequest) m;
            String password = getProxy().getPasswordCache().get(l.getUserName());
            FacilityConfig f = m.getFacility();
            if (password == null) {
                // We need a password to successfully log out of the ACLS server. 
                // So if there isn't one we have to pretend we logged out.
                getLogger().debug("No cached password found for " + l.getUserName());
                r = new AllowedResponse(ResponseType.LOGOUT_ALLOWED);
            } else {
                Request vl = new LogoutRequest(RequestType.VIRTUAL_LOGOUT, 
                        l.getUserName(), password, l.getAccount(), f, null, null);
                Response vr = getClient().serverSendReceive(vl);
                switch (vr.getType()) {
                case VIRTUAL_LOGOUT_ALLOWED:
                    r = new AllowedResponse(ResponseType.LOGOUT_ALLOWED);
                    break;
                case VIRTUAL_LOGOUT_REFUSED:
                    r = new RefusedResponse(ResponseType.LOGOUT_REFUSED);
                    break;
                case COMMAND_ERROR:
                    r = vr;
                    break;
                default:
                    throw new AclsProtocolException(
                            "Unexpected response for virtual logout: " + vr.getType());
                }
            }
            // (Issue a logout event, even if the logout request was refused, or 
            // we didn't even send it.)
            getProxy().sendEvent(new AclsLogoutEvent(f, l.getUserName(), l.getAccount()));
        } else {
            r = new RefusedResponse(ResponseType.LOGOUT_REFUSED);
        }
        return r;
    }

    protected Response processLoginRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            // Turn a 'login' request into a 'virtual_login' request, and 
            // map the response to the appropriate 'login' response.
            LoginRequest l = (LoginRequest) m;
            Request vl = new LoginRequest(RequestType.VIRTUAL_LOGIN, 
                    l.getUserName(), l.getPassword(), m.getFacility(), null, null);
            try {
                Response vr = getClient().serverSendReceive(vl);
                switch (vr.getType()) {
                case VIRTUAL_LOGIN_ALLOWED:
                    LoginResponse vlr = (LoginResponse) vr;
                    getProxy().sendEvent(new AclsPasswordAcceptedEvent(m.getFacility(),
                            new AclsLoginDetails(l.getUserName(), vlr.getUserName(),
                                    vlr.getOrgName(), l.getPassword(),
                                    m.getFacility().getFacilityName(), vlr.getAccounts(), 
                                    vlr.getCertification(), vlr.isOnsiteAssist(), false)));
                    getProxy().getPasswordCache().put(l.getUserName(), l.getPassword());
                    getLogger().debug("Cached password for " + l.getUserName());
                    r = new LoginResponse(ResponseType.LOGIN_ALLOWED, 
                            vlr.getUserName(), vlr.getOrgName(), vlr.getLoginTimestamp(),
                            vlr.getAccounts(), vlr.getCertification(), vlr.isOnsiteAssist());
                    break;
                case VIRTUAL_LOGIN_REFUSED:
                    r = new RefusedResponse(ResponseType.LOGIN_REFUSED);
                    break;
                case COMMAND_ERROR:
                    r = vr;
                    break;
                default:
                    throw new AclsProtocolException(
                            "Unexpected response for virtual login: " + vr.getType());
                }
            } catch (AclsCommsException ex) {
                r = tryFallbackAuthentication(l);
            }
        } else {
            r = new RefusedResponse(ResponseType.LOGIN_REFUSED);
        }
        return r;
    }
}
