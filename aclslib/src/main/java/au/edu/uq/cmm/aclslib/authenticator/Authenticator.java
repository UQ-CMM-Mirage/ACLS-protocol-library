package au.edu.uq.cmm.aclslib.authenticator;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;

/**
 * The Authenticator class uses an ACLS server as a means of checking a
 * username and password.  This requires a registered dummy facility 
 * that we can "login" to.
 * 
 * @author scrawley
 */
public class Authenticator {
    
    private static final Logger LOG = Logger.getLogger(Authenticator.class);
    private AclsClient client;
    private StaticFacilityConfig dummyFacility;
    private boolean useVirtual;

    public Authenticator(String serverHost, int serverPort, String dummyFacilityName) {
        this.client = new AclsClient(serverHost, serverPort);
        dummyFacility = new StaticFacilityConfig();
        dummyFacility.setFacilityName(dummyFacilityName);
        useVirtual = checkForVmflCapablility();
    }

    public boolean authenticate(String userName, String password) throws AclsException {
        if (useVirtual) {
            return virtualFacilityLogin(userName, password);
        } else {
            return realFacilityLogin(userName, password);
        }
    }
    
    private boolean checkForVmflCapablility() {
        LOG.debug("Checking for vMFL capability");
        try {
            Request request = new SimpleRequest(RequestType.USE_VIRTUAL, null, null, null);
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case USE_VIRTUAL:
                YesNoResponse uv = (YesNoResponse) response;
                LOG.info("The 'useVirtual' request returned " + uv.isYes());
                return uv.isYes();
            default:
                throw new AclsProtocolException(
                        "Unexpected response to USE_VIRTUAL request - " + 
                                response.getType());
            }
        } catch (AclsException ex) {
            // We do this in case we are talking to a server that is not
            // aware of the vMFL requests.
            LOG.info("The 'useVirtual' request failed - assuming no vMFL", ex);
            return false;
        }
    }

    private boolean virtualFacilityLogin(String userName, String password) throws AclsException {
        Request request = new LoginRequest(
                RequestType.VIRTUAL_LOGIN, userName, password, dummyFacility, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case VIRTUAL_LOGIN_ALLOWED:
            return true;
        case VIRTUAL_LOGIN_REFUSED:
            return false;
        default:
            throw new AclsProtocolException(
                    "Unexpected response to VIRTUAL_LOGIN request - " + 
                    response.getType());
        }
    }
    
    private boolean realFacilityLogin(String userName, String password) throws AclsException {
        Request request = new LoginRequest(
                RequestType.LOGIN, userName, password, null, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case LOGIN_ALLOWED:
            return true;
        case LOGIN_REFUSED:
            return false;
        default:
            throw new AclsProtocolException(
                    "Unexpected response to LOGIN request - " + 
                    response.getType());
        }
    }
}
