package au.edu.uq.cmm.aclslib.proxy;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.config.Configuration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.StaticConfiguration;
import au.edu.uq.cmm.aclslib.message.AccountRequest;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ServerStatusException;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
import au.edu.uq.cmm.aclslib.server.RequestListener;
import au.edu.uq.cmm.aclslib.server.RequestProcessorFactory;
import au.edu.uq.cmm.aclslib.service.CompositeServiceBase;
import au.edu.uq.cmm.aclslib.service.Service;
import au.edu.uq.cmm.aclslib.service.ServiceException;


/**
 * 
 */
public class AclsProxy extends CompositeServiceBase {
    private static final Logger LOG = Logger.getLogger(AclsProxy.class);
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
  

    public AclsProxy(Configuration config) {
        this.config = config;
        try {
            this.requestListener = new RequestListener(config, config.getProxyPort(),
                    config.getProxyHost(),
                    new RequestProcessorFactory() {
                public Runnable createProcessor(Configuration config, Socket s) {
                    if (config.isUseVmfl()) {
                        return new VmflRequestProcessor(config, s, AclsProxy.this);
                    } else {
                        return new NormalRequestProcessor(config, s, AclsProxy.this);
                    }
                }
            });
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Configuration problem", ex);
        }
        if (config.isUseVmfl()) {
            this.facilityChecker = new FacilityChecker(config);
        }
    }

    public static void main(String[] args) {
        String configFile = null;
        if (args.length > 0) {
            configFile = args[0];
        }
        try {
            Configuration config = StaticConfiguration.loadConfiguration(configFile);
            if (config == null) {
                LOG.info("Can't read/load proxy configuration file");
                System.exit(2);
            }
            AclsProxy proxy = new AclsProxy(config);
            try {
                proxy.probeServer();
            } catch (ServiceException ex) {
                LOG.error("Cannot contact ACLS server", ex);
                System.exit(3);
            }
            proxy.addListener(new AclsFacilityEventListener() {
                public void eventOccurred(AclsFacilityEvent event) {
                    LOG.info("Facility event: " + event);
                }
            });
            proxy.startup();
            proxy.awaitShutdown();
            LOG.info("Exitting normally");
            System.exit(0);
        } catch (Throwable ex) {
            LOG.error("Unhandled exception", ex);
            System.exit(1);
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
            if (config.isUseVmfl()) {
                vmflProbe(client);
            } else {
                regularProbe(client);
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

    private void regularProbe(AclsClient client) throws AclsException {
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
    }

    private void vmflProbe(AclsClient client) throws AclsException {
        Request request = new SimpleRequest(RequestType.USE_VIRTUAL, null, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case USE_VIRTUAL:
            YesNoResponse uv = (YesNoResponse) response;
            if (!uv.isYes()) {
                throw new ServiceException(
                        "The ACLS server has the proxy configured " +
                        "as a normal Facility");
            }
            break;
        default:
            LOG.error("Unexpected response for USE_VIRTUAL request: " + 
                    response.getType());
            throw new ServiceException(
                    "The ACLS server gave an unexpected response " +
                    "to our probe (see log)");
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
    throws AclsLoginException {
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
        Request request = new LoginRequest(
                config.isUseVmfl() ? RequestType.VIRTUAL_LOGIN : RequestType.LOGIN, 
                userName, password, facility, null, null);
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case LOGIN_ALLOWED:
            case VIRTUAL_LOGIN_ALLOWED:
                return ((LoginResponse) response).getAccounts();
            case LOGIN_REFUSED:
            case VIRTUAL_LOGIN_REFUSED:
                throw new AclsLoginException(
                        "Login refused - username or password supplied is incorrect");
            default:
                LOG.error("Unexpected response - " + response.getType());
                throw new AclsLoginException(
                        "Internal error - see server logs for details");
            }
        } catch (AclsException ex) {
            LOG.error(ex);
            throw new AclsLoginException(
                    "Internal error - see server logs for details");
        }
    }

    public void selectAccount(FacilityConfig facility, String userName, String account) 
    throws AclsLoginException {
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
        Request request = new AccountRequest(
                config.isUseVmfl() ? RequestType.VIRTUAL_ACCOUNT : RequestType.ACCOUNT,
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
                throw new AclsLoginException("Account selection refused");
            default:
                LOG.error("Unexpected response - " + response.getType());
                throw new AclsLoginException(
                        "Internal error - see server logs for details");
            }
        } catch (AclsException ex) {
            LOG.error(ex);
            throw new AclsLoginException(
                    "Internal error - see server logs for details");
        }
    }
}
