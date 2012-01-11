package au.edu.uq.cmm.aclslib.authenticator;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsProtocolException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
import au.edu.uq.cmm.aclslib.server.Configuration;
import au.edu.uq.cmm.aclslib.server.StaticConfiguration;

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
    private String dummyFacility;

    public Authenticator(String serverHost, int serverPort, String dummyFacility) {
        this.client = new AclsClient(serverHost, serverPort);
        this.dummyFacility = dummyFacility;
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            System.err.println("usage: <config-file> <user> <password>");
            System.exit(1);
        }
        String configFile = args[0];
        String user = args[1];
        String password = args[2];
        try {
            Configuration config = StaticConfiguration.loadConfiguration(configFile);
            if (config == null) {
                LOG.info("Can't read/load proxy configuration file");
                System.exit(2);
            }
            Authenticator authenticator = new Authenticator(
                    config.getServerHost(), config.getServerPort(), 
                    config.getDummyFacility());
            boolean ok = authenticator.authenticate(user, password);
            if (ok) {
                System.out.println("Credentials accepted");
            } else {
                System.out.println("Credentials rejected");
            }
        } catch (Throwable ex) {
            LOG.error("Unhandled exception", ex);
            System.exit(1);
        }
    }

    public boolean authenticate(String userName, String password) {
        if (useVirtual()) {
            return virtualFacilityLogin(userName, password);
        } else {
            return realFacilityLogin(userName, password);
        }
    }
    
    private boolean useVirtual() {
        Request request = new SimpleRequest(RequestType.USE_VIRTUAL);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case USE_VIRTUAL:
            YesNoResponse uv = (YesNoResponse) response;
            return uv.isYes();
        default:
            throw new AclsProtocolException(
                    "Unexpected response to USE_VIRTUAL request - " + 
                            response.getType());
        }
    }

    private boolean virtualFacilityLogin(String userName, String password) {
        Request request = new LoginRequest(
                RequestType.VIRTUAL_LOGIN, userName, password, dummyFacility);
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
    
    private boolean realFacilityLogin(String userName, String password) {
        Request request = new LoginRequest(
                RequestType.LOGIN, userName, password, null);
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
