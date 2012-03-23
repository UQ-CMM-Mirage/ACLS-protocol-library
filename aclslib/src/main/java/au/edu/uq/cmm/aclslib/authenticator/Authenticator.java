package au.edu.uq.cmm.aclslib.authenticator;


import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
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
public class Authenticator {
    private final AclsClient client;
    private final StaticFacilityConfig dummyFacility;
    private final boolean useVirtual;

    public Authenticator(String serverHost, int serverPort, String dummyFacilityName) {
        client = new AclsClient(serverHost, serverPort);
        dummyFacility = new StaticFacilityConfig();
        dummyFacility.setFacilityName(dummyFacilityName);
        useVirtual = client.checkForVmflSupport();
    }

    public boolean authenticate(String userName, String password) throws AclsException {
        if (useVirtual) {
            return vmflLogin(userName, password);
        } else {
            return login(userName, password);
        }
    }

    private boolean vmflLogin(String userName, String password) throws AclsException {
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
    
    private boolean login(String userName, String password) throws AclsException {
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
