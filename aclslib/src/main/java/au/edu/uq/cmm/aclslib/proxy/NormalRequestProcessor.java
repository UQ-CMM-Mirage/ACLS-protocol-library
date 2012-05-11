/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.proxy;

import java.net.Socket;
import java.util.Date;

import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.authenticator.AclsLoginDetails;
import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AccountRequest;
import au.edu.uq.cmm.aclslib.message.AccountResponse;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AclsNoResponseException;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.AllowedResponse;
import au.edu.uq.cmm.aclslib.message.FacilityNameResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.LogoutRequest;
import au.edu.uq.cmm.aclslib.message.NoteRequest;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;

/**
 * @author scrawley
 */
public class NormalRequestProcessor extends ProxyRequestProcessor {
    
    public NormalRequestProcessor(
            ACLSProxyConfiguration config, FacilityMapper mapper,
            Socket socket, AclsProxy proxy) {
        super(config, mapper, 
                LoggerFactory.getLogger(NormalRequestProcessor.class), 
                socket, proxy);
    }

    @Override
    protected Response processFacilityRequest(Request m) 
            throws AclsException {
        if (m.getFacility() == null) {
            return new RefusedResponse(ResponseType.FACILITY_REFUSED);
        }
        try {
            Request fr = new SimpleRequest(RequestType.FACILITY_NAME, 
                    m.getFacility(), null, m.getFacility().getLocalHostId());
            return getClient().serverSendReceive(fr);
        } catch (AclsNoResponseException ex) {
            return new FacilityNameResponse(m.getFacility().getFacilityDescription() + " (cached)");
        }
    }

    @Override
    protected Response processNotesRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            NoteRequest nr = (NoteRequest) m;
            String notes = nr.getNotes();
            Request vnr = new NoteRequest(nr.getUserName(), nr.getAccount(), notes, 
                    m.getFacility(), null, nr.getLocalHostId());
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

    @Override
    protected Response processAccountRequest(Request m) throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            AccountRequest a = (AccountRequest) m;
            Request vl = new AccountRequest(RequestType.ACCOUNT,
                    a.getUserName(), a.getAccount(), m.getFacility(), null,
                    a.getLocalHostId());
            try {
                r = getClient().serverSendReceive(vl);
                switch (r.getType()) {
                case ACCOUNT_ALLOWED:
                    getProxy().sendEvent(
                            new AclsLoginEvent(m.getFacility(),
                                    a.getUserName(), a.getAccount()));
                    break;
                case LOGOUT_REFUSED:
                case COMMAND_ERROR:
                    break;
                default:
                    throw new AclsProtocolException(
                            "Unexpected response for account: " + r.getType());
                }
            } catch (AclsNoResponseException ex) {
                r = new AccountResponse(ResponseType.ACCOUNT_ALLOWED, new Date().toString());
            }
        } else {
            r = new RefusedResponse(ResponseType.ACCOUNT_REFUSED);
        }
        return r;
    }

    @Override
    protected Response processLogoutRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            LogoutRequest l = (LogoutRequest) m;
            Request vl = new LogoutRequest(RequestType.LOGOUT, 
                    l.getUserName(), null, l.getAccount(), 
                    m.getFacility(), null, l.getLocalHostId());
            try {
                r = getClient().serverSendReceive(vl);
                switch (r.getType()) {
                case LOGOUT_ALLOWED:
                case LOGOUT_REFUSED:
                case COMMAND_ERROR:
                    break;
                default:
                    throw new AclsProtocolException(
                            "Unexpected response for logout: " + r.getType());
                }
            } catch (AclsNoResponseException ex) {
                r = new AllowedResponse(ResponseType.LOGOUT_ALLOWED);
            }
            // Issue a logout event, even if the logout request was refused.
            getProxy().sendEvent(
                    new AclsLogoutEvent(m.getFacility(), l.getUserName(), l.getAccount()));
        } else {
            r = new RefusedResponse(ResponseType.LOGOUT_REFUSED);
        }
        return r;
    }

    @Override
    protected Response processLoginRequest(Request m) 
            throws AclsException {
        Response r;
        if (m.getFacility() != null) {
            LoginRequest l = (LoginRequest) m;
            Request vl = new LoginRequest(RequestType.LOGIN, 
                    l.getUserName(), l.getPassword(), 
                    m.getFacility(), null, l.getLocalHostId());
            try {
                r = getClient().serverSendReceive(vl);
                switch (r.getType()) {
                case LOGIN_ALLOWED:
                    LoginResponse lr = (LoginResponse) r;
                    getProxy().sendEvent(new AclsPasswordAcceptedEvent(m.getFacility(),
                            new AclsLoginDetails(l.getUserName(), lr.getUserName(),
                                    lr.getOrgName(), l.getPassword(),
                                    m.getFacility().getFacilityName(), lr.getAccounts(), 
                                    lr.getCertification(), lr.isOnsiteAssist(), false)));
                    break;
                case LOGIN_REFUSED:
                case COMMAND_ERROR:
                    break;
                default:
                    throw new AclsProtocolException(
                            "Unexpected response for login: " + r.getType());
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
