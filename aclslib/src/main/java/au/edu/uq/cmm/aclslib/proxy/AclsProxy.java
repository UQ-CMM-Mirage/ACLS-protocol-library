package au.edu.uq.cmm.aclslib.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import au.edu.uq.cmm.aclslib.config.Configuration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.StaticConfiguration;
import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
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
                    return new RequestProcessor(config, s, AclsProxy.this);
                }
            });
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Configuration problem", ex);
        }
        this.facilityChecker = new FacilityChecker(config);
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--createSampleConfig")) {
            createSampleConfigurationFile();
            return;
        }
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
        facilityChecker.shutdown();
        requestListener.shutdown();
        LOG.info("Shutdown completed");
    }

    @Override
    protected void doStartup() throws ServiceException, InterruptedException {
        LOG.info("Starting up");
        requestListener.startup();
        facilityChecker.startup();
        LOG.info("Startup completed");
    }

    public void probeServer() throws ServiceException {
        LOG.info("Probing ACLS server");
        AclsClient client = new AclsClient(config.getServerHost(), config.getServerPort());
        Request request = new SimpleRequest(RequestType.USE_VIRTUAL);
        Response response;
        try {
            response = client.serverSendReceive(request);
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
        switch (response.getType()) {
        case USE_VIRTUAL:
            YesNoResponse uv = (YesNoResponse) response;
            if (!uv.isYes()) {
                throw new ServiceException(
                        "The ACLS server has the proxy configured as a normal Facility");
            }
            break;
        default:
            LOG.error("Unexpected response for USE_VIRTUAL request: " + response.getType());
            throw new ServiceException(
                    "The ACLS server gave an unexpected response to our probe (see log)");
        }
    }

    public static void createSampleConfigurationFile() {
        StaticConfiguration sampleConfig = new StaticConfiguration();
        sampleConfig.setServerHost("aclsHost.example.com");
        sampleConfig.setProxyHost("proxyHost.example.com");
        Map<String, StaticFacilityConfig> facilityMap =
                new TreeMap<String, StaticFacilityConfig>();
        sampleConfig.setFacilities(facilityMap);
        StaticFacilityConfig f1 = new StaticFacilityConfig();
        f1.setAccessName("jim");
        f1.setAccessPassword("secret");
        f1.setFolderName("/trollscope");
        f1.setDriveName("T");
        f1.setFacilityName("F001");
        f1.setFacilityDescription("Trollscope 2000T");
        f1.setUseFullScreen(true);
        facilityMap.put("192.168.1.1", f1);
        StaticFacilityConfig f2 = new StaticFacilityConfig();
        f2.setFacilityName("F002");
        f2.setFacilityDescription("The hatstand in the corner");
        f2.setUseFullScreen(false);
        facilityMap.put("hatstand.example.com", f2);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory jf = new JsonFactory();
            JsonGenerator jg = jf.createJsonGenerator(System.out);
            jg.useDefaultPrettyPrinter();
            mapper.writeValue(jg, sampleConfig);
        } catch (JsonParseException e) {
            LOG.error(e);
        } catch (JsonMappingException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
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

    public void localLogin(FacilityConfig facility, String userName, String password) 
    throws AclsLoginException {
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort());
        Request request = new LoginRequest(
                RequestType.VIRTUAL_LOGIN, userName, password, facility.getFacilityName());
        try {
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case VIRTUAL_LOGIN_ALLOWED:
                sendEvent(new AclsLoginEvent(facility, userName, "unspecified"));
                return;
            case VIRTUAL_LOGIN_REFUSED:
                throw new AclsLoginException(
                        "Login refused - username or password incorrect");
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
