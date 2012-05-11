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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.message.AccountRequest;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.LogoutRequest;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ServerStatusException;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.service.ServiceException;


/**
 * @author scrawley
 */
public class AclsHelper {
    private static final Logger LOG = LoggerFactory.getLogger(AclsHelper.class);
    private boolean useVmfl;
    private String host;
    private int port;
    private int timeout;
  

    /**
     * Instantiate the helper.
     * 
     * @param host the ACLS server or proxy to talk to
     * @param port the server / proxy's ACLS port number.
     * @param vmflCheck if true, check to see if we should enable vMFL by sending a
     *     vMFL specific request.  If false, vMFL won't be used.
     */
    public AclsHelper(String host, int port, int timeout, boolean vmflCheck) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.useVmfl = vmflCheck && 
                new AclsClient(host, port, timeout).checkForVmflSupport();
    }

    public void probeServer() throws ServiceException {
        LOG.info("Probing ACLS server");
        AclsClient client = new AclsClient(host, port, timeout);
        try {
            Request request = new SimpleRequest(RequestType.USE_PROJECT, null, null, null);
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case PROJECT_NO:
            case PROJECT_YES:
                break;
            default:
                LOG.error("Unexpected response for USE_PROJECT request: " + 
                        response.getType());
                throw new ServiceException(
                        "The ACLS server gave an unexpected response " +
                        "to our probe (see log)");
            }
        } catch (AclsCommsException ex) {
            throw new ServiceException(
                    "The ACLS server is not responding", ex);
        } catch (ServerStatusException ex) {
            throw new ServiceException(
                    "The ACLS server rejected our probe", ex);
        } catch (AclsException ex) {
            throw new ServiceException(
                    "The ACLS server is not behaving correctly", ex);
        } 
    }

    public List<String> login(FacilityConfig facility, String userName, String password) 
    throws AclsAuthenticationException {
        AclsClient client = new AclsClient(host, port, timeout);
        Request request = new LoginRequest(
                useVmfl ? RequestType.VIRTUAL_LOGIN : RequestType.LOGIN, 
                userName, password, facility, null, null);
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case LOGIN_ALLOWED:
                return ((LoginResponse) response).getAccounts();
            case VIRTUAL_LOGIN_ALLOWED:
                LOG.debug("Cached password for " + userName);
                return ((LoginResponse) response).getAccounts();
            case LOGIN_REFUSED:
            case VIRTUAL_LOGIN_REFUSED:
                throw new AclsAuthenticationException(
                        "Login refused - username or password supplied is incorrect");
            default:
                LOG.error("Unexpected response - " + response.getType());
                throw new AclsAuthenticationException(
                        "Internal error - see server logs for details");
            }
        } catch (AclsException ex) {
            LOG.error("Internal error", ex);
            throw new AclsAuthenticationException(
                    "Internal error - see server logs for details");
        }
    }

    public void selectAccount(FacilityConfig facility, String userName, String account) 
    throws AclsAuthenticationException {
        AclsClient client = new AclsClient(host, port, timeout);
        Request request = new AccountRequest(
                useVmfl ? RequestType.VIRTUAL_ACCOUNT : RequestType.ACCOUNT,
                userName, account, facility, null, null);
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case ACCOUNT_ALLOWED:
            case VIRTUAL_ACCOUNT_ALLOWED:
                //sendEvent(new AclsLoginEvent(facility, userName, account));
                return;
            case ACCOUNT_REFUSED:
            case VIRTUAL_ACCOUNT_REFUSED:
                LOG.error("Account selection refused");
                throw new AclsAuthenticationException("Account selection refused");
            default:
                LOG.error("Unexpected response - " + response.getType());
                throw new AclsAuthenticationException(
                        "Internal error - see server logs for details");
            }
        } catch (AclsException ex) {
            LOG.error("Internal error", ex);
            throw new AclsAuthenticationException(
                    "Internal error - see server logs for details");
        }
    }

    public void logout(FacilityConfig facility, String userName, String account) 
            throws AclsAuthenticationException {
        AclsClient client = new AclsClient(host, port, timeout);
        Request request;
        if (useVmfl) {
            request = new LogoutRequest(
                RequestType.VIRTUAL_LOGOUT,
                userName, "", account, facility, null, null);
        } else {
            request = new LogoutRequest(
                    RequestType.LOGOUT,
                    userName, null, account, facility, null, null);
        }
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case LOGOUT_ALLOWED:
            case VIRTUAL_LOGOUT_ALLOWED:
                //sendEvent(new AclsLogoutEvent(facility, userName, account));
                return;
            case LOGOUT_REFUSED:
            case VIRTUAL_LOGOUT_REFUSED:
                LOG.error("Logout refused");
                throw new AclsAuthenticationException("Logout refused");
            default:
                LOG.error("Unexpected response - " + response.getType());
                throw new AclsAuthenticationException(
                        "Internal error - see server logs for details");
            }
        } catch (AclsException ex) {
            LOG.error("Internal error", ex);
            throw new AclsAuthenticationException(
                    "Internal error - see server logs for details");
        }
    }
}
