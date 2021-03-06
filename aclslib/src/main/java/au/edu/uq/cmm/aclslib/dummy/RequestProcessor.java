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

package au.edu.uq.cmm.aclslib.dummy;

import java.io.BufferedWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AccountResponse;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AllowedResponse;
import au.edu.uq.cmm.aclslib.message.Certification;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.SystemPasswordResponse;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
import au.edu.uq.cmm.aclslib.proxy.AclsProxy;
import au.edu.uq.cmm.aclslib.server.RequestProcessorBase;

/**
 * The request processor component for the dummy ACLS server.  This version
 * supports only the subset used by a vMFL client.
 * 
 * FIXME - it doesn't don't support FACILITY_COUNT or FACILITY_LIST which the
 * proxy now uses.
 * 
 * @author scrawley
 */
public class RequestProcessor extends RequestProcessorBase implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AclsProxy.class);
    
    public RequestProcessor(ACLSProxyConfiguration config, 
            FacilityMapper facilityMapper, Socket socket) {
        super(config, facilityMapper, 
                LoggerFactory.getLogger(RequestProcessor.class), socket);
    }

    protected void doProcess(Request m, BufferedWriter w) 
            throws AclsException {
        LOG.debug("Request is " + m.getType().name() + "(" + m.unparse(true) + ")");
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
 
    private void processLoginRequest(Request m, BufferedWriter w) 
            throws AclsException {
        LoginRequest login = (LoginRequest) m;
        String timestamp = DateFormat.getInstance().format(new Date());
        List<String> accounts = Arrays.asList("general", "special");
        Response r;
        if (!login.getPassword().equals("secret")) {
            r = new RefusedResponse(ResponseType.VIRTUAL_LOGIN_REFUSED);
        } else if (login.getUserName().equals("junior")) {
            r = new LoginResponse(ResponseType.VIRTUAL_LOGIN_ALLOWED, 
                    login.getUserName(), "CMMMM", timestamp, accounts,
                    Certification.NONE, true);
        } else if (login.getUserName().equals("badboy")) {
            r = new LoginResponse(ResponseType.VIRTUAL_LOGIN_ALLOWED, 
                    login.getUserName(), "CMMMM", timestamp, accounts,
                    Certification.NONE, false);
        } else {
            r = new LoginResponse(ResponseType.VIRTUAL_LOGIN_ALLOWED, 
                    login.getUserName(), "CMMMM", timestamp, accounts,
                    Certification.VALID, false);
        }
        sendResponse(w, r);
    }

    private void processLogoutRequest(Request m, BufferedWriter w) 
            throws AclsException {
        Response r = new AllowedResponse(ResponseType.VIRTUAL_LOGOUT_ALLOWED);
        sendResponse(w, r);
    }

    private void processAccountRequest(Request m, BufferedWriter w) 
            throws AclsException {
        String timestamp = DateFormat.getInstance().format(new Date());
        Response r = new AccountResponse(ResponseType.VIRTUAL_ACCOUNT_ALLOWED, timestamp);
        sendResponse(w, r);
    }

    private void processNotesRequest(Request m, BufferedWriter w)
            throws AclsException {
        Response r = new AllowedResponse(ResponseType.NOTES_ALLOWED);
        sendResponse(w, r);
    }

    private void processUseProjectRequest(Request m, BufferedWriter w) 
            throws AclsException {
        Response r = new YesNoResponse(ResponseType.PROJECT_YES, true);
        sendResponse(w, r);
    }

    private void processUseVirtualRequest(Request m, BufferedWriter w) 
            throws AclsException {
        Response r = new YesNoResponse(ResponseType.USE_VIRTUAL, true);
        sendResponse(w, r);
    }

    private void processStaffLoginRequest(Request m, BufferedWriter w) 
            throws AclsException {
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
            throws AclsException {
        Response r = new SystemPasswordResponse("secret");
        sendResponse(w, r);
    }
}
