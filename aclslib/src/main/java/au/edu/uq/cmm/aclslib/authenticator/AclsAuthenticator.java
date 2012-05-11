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

package au.edu.uq.cmm.aclslib.authenticator;


import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;

/**
 * The Authenticator class uses an ACLS server as a means of checking a
 * username and password.  This requires a registered dummy facility 
 * that we can "login" to.
 * 
 * @author scrawley
 */
public class AclsAuthenticator implements Authenticator {
    private final AclsClient client;
    private final StaticFacilityConfig dummyFacility;
    private final boolean useVirtual;

    public AclsAuthenticator(String serverHost, int serverPort, 
            int timeout, String dummyFacilityName, String localHostId) {
        client = new AclsClient(serverHost, serverPort, timeout);
        dummyFacility = new StaticFacilityConfig();
        dummyFacility.setFacilityName(dummyFacilityName);
        dummyFacility.setLocalHostId(localHostId);
        useVirtual = client.checkForVmflSupport();
    }

    public AclsLoginDetails authenticate(
            String userName, String password, FacilityConfig dummy) 
            throws AclsException {
        if (useVirtual) {
            return vmflLogin(userName, password);
        } else {
            return login(userName, password);
        }
    }

    private AclsLoginDetails vmflLogin(String userName, String password) 
             throws AclsException {
        Request request = new LoginRequest(
                RequestType.VIRTUAL_LOGIN, userName, password, dummyFacility, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case VIRTUAL_LOGIN_ALLOWED:
            LoginResponse lr = (LoginResponse) response;
            return new AclsLoginDetails(userName, lr.getUserName(), lr.getOrgName(), password, "",
                    lr.getAccounts(), lr.getCertification(), lr.isOnsiteAssist(), false);
        case VIRTUAL_LOGIN_REFUSED:
            return null;
        default:
            throw new AclsProtocolException(
                    "Unexpected response to VIRTUAL_LOGIN request - " + 
                    response.getType());
        }
    }
    
    private AclsLoginDetails login(String userName, String password) throws AclsException {
        Request request = new LoginRequest(
                RequestType.LOGIN, userName, password, dummyFacility, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case LOGIN_ALLOWED:
            LoginResponse lr = (LoginResponse) response;
            return new AclsLoginDetails(userName, lr.getUserName(), lr.getOrgName(), password, "",
                    lr.getAccounts(), lr.getCertification(), lr.isOnsiteAssist(), false);
        case LOGIN_REFUSED:
            return null;
        default:
            throw new AclsProtocolException(
                    "Unexpected response to LOGIN request - " + 
                    response.getType());
        }
    }
}
