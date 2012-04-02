package au.edu.uq.cmm.aclslib.proxy;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.*;

import au.edu.uq.cmm.aclslib.config.Configuration;
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
import au.edu.uq.cmm.aclslib.server.RequestListener;
import au.edu.uq.cmm.aclslib.server.RequestProcessorFactory;
import au.edu.uq.cmm.aclslib.service.CompositeServiceBase;
import au.edu.uq.cmm.aclslib.service.Service;
import au.edu.uq.cmm.aclslib.service.ServiceException;


/**
 * 
 */
public class AclsProxy extends CompositeServiceBase {
    private static final Logger LOG = LoggerFactory.getLogger(AclsProxy.class);
    private Configuration config;
    private Service requestListener;
    private Service facilityChecker;
    private List<AclsFacilityEventListener> listeners = 
            new ArrayList<AclsFacilityEventListener>();
    // The virtual logout requests requires a password (!?!), so we've 
    // got no choice but to remember it.
    // FIXME - this will need to be persisted if sessions are to survive 
    // beyond a restart.
    private Map<String, String> passwordCache = new HashMap<String, String>();
    private boolean useVmfl;
  

    public AclsProxy(Configuration config) {
        this.config = config;
        try {
            this.useVmfl = new AclsClient(config.getServerHost(),
                    config.getServerPort()).checkForVmflSupport();
            this.requestListener = new RequestListener(config, config.getProxyPort(),
                    config.getProxyHost(),
                    new RequestProcessorFactory() {
                public Runnable createProcessor(Configuration config, Socket s) {
                    if (useVmfl) {
                        return new VmflRequestProcessor(config, s, AclsProxy.this);
                    } else {
                        return new NormalRequestProcessor(config, s, AclsProxy.this);
                    }
                }
            });
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Configuration problem", ex);
        }
        if (useVmfl) {
            this.facilityChecker = new FacilityChecker(config);
        }
    }

    @Override
    protected void doShutdown() throws ServiceException, InterruptedException {
        LOG.info("Shutting down");
        if (facilityChecker != null) {
            facilityChecker.shutdown();
        }
        requestListener.shutdown();
        LOG.info("Shutdown completed");
    }

    @Override
    protected void doStartup() throws ServiceException, InterruptedException {
        LOG.info("Starting up");
        requestListener.startup();
        if (facilityChecker != null) {
            facilityChecker.startup();
        }
        LOG.info("Startup completed");
    }

    public void probeServer() throws ServiceException {
        LOG.info("Probing ACLS server");
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
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

    public void sendEvent(AclsFacilityEvent event) {
        synchronized (listeners) {
            for (AclsFacilityEventListener listener : listeners) {
                listener.eventOccurred(event);
            }
        }
    }

    public void addListener(AclsFacilityEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(AclsFacilityEventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public Map<String, String> getPasswordCache() {
        return passwordCache;
    }

    public List<String> login(FacilityConfig facility, String userName, String password) 
    throws AclsAuthenticationException {
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
        Request request = new LoginRequest(
                useVmfl ? RequestType.VIRTUAL_LOGIN : RequestType.LOGIN, 
                userName, password, facility, null, null);
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case LOGIN_ALLOWED:
                return ((LoginResponse) response).getAccounts();
            case VIRTUAL_LOGIN_ALLOWED:
                passwordCache.put(userName, password);
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
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
        Request request = new AccountRequest(
                useVmfl ? RequestType.VIRTUAL_ACCOUNT : RequestType.ACCOUNT,
                userName, account, facility, null, null);
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case ACCOUNT_ALLOWED:
            case VIRTUAL_ACCOUNT_ALLOWED:
                sendEvent(new AclsLoginEvent(facility, userName, account));
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
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
        Request request;
        if (useVmfl) {
            String password = passwordCache.get(userName);
            request = new LogoutRequest(
                RequestType.VIRTUAL_LOGOUT,
                userName, password == null ? "" : password, account, facility, null, null);
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
                sendEvent(new AclsLogoutEvent(facility, userName, account));
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
