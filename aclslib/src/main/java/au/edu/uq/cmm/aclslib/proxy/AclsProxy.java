package au.edu.uq.cmm.aclslib.proxy;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
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
 * @author scrawley
 */
public class AclsProxy extends CompositeServiceBase {
    private static final Logger LOG = LoggerFactory.getLogger(AclsProxy.class);
    private ACLSProxyConfiguration config;
    private Service requestListener;
    private List<AclsFacilityEventListener> listeners = 
            new ArrayList<AclsFacilityEventListener>();
    // The virtual logout requests requires a password (!?!), so we've 
    // got no choice but to remember it.
    // FIXME - this will need to be persisted if sessions are to survive 
    // beyond a restart.
    private Map<String, String> passwordCache = new HashMap<String, String>();
    private final boolean useVmfl;
  

    public AclsProxy(ACLSProxyConfiguration config, FacilityMapper mapper) {
        this.config = config;
        try {
            this.useVmfl = new AclsClient(config.getServerHost(),
                    config.getServerPort()).checkForVmflSupport();
            this.requestListener = new RequestListener(config, mapper,
                    config.getProxyPort(), config.getProxyHost(),
                    new RequestProcessorFactory() {
                public Runnable createProcessor(ACLSProxyConfiguration config, FacilityMapper mapper, Socket s) {
                    if (useVmfl) {
                        return new VmflRequestProcessor(config, mapper, s, AclsProxy.this);
                    } else {
                        return new NormalRequestProcessor(config, mapper, s, AclsProxy.this);
                    }
                }
            });
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Configuration problem", ex);
        }
    }

    @Override
    protected void doShutdown() throws ServiceException, InterruptedException {
        LOG.info("Shutting down");
        requestListener.shutdown();
        LOG.info("Shutdown completed");
    }

    @Override
    protected void doStartup() throws ServiceException, InterruptedException {
        LOG.info("Starting up");
        requestListener.startup();        LOG.info("Startup completed");
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
}
