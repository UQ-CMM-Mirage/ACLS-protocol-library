package au.edu.uq.cmm.aclslib.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;
import au.edu.uq.cmm.aclslib.server.Configuration;
import au.edu.uq.cmm.aclslib.server.Facility;
import au.edu.uq.cmm.aclslib.server.RequestListener;
import au.edu.uq.cmm.aclslib.server.RequestProcessorFactory;
import au.edu.uq.cmm.aclslib.server.ServerException;


/**
 * 
 */
public class AclsProxy {
    private static final Logger LOG = Logger.getLogger(AclsProxy.class);
    private Configuration config;
    private Thread requestListenerThread;
    private List<AclsFacilityEventListener> listeners = 
            new ArrayList<AclsFacilityEventListener>();
    
    public AclsProxy(Configuration config) {
        this.config = config;
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
            Configuration config = Configuration.loadConfiguration(configFile);
            if (config == null) {
                LOG.info("Can't read/load proxy configuration file");
                System.exit(2);
            }
            AclsProxy proxy = new AclsProxy(config);
            try {
                proxy.probeServer();
            } catch (ServerException ex) {
                LOG.error("Cannot contact ACLS server", ex);
                System.exit(3);
            }
            proxy.addListener(new AclsFacilityEventListener() {
                public void eventOccurred(AclsFacilityEvent event) {
                    LOG.info("Facility event: " + event);
                }
            });
            proxy.startup();
            proxy.shutdown();
            LOG.info("Exitting normally");
            System.exit(0);
        } catch (Throwable ex) {
            LOG.error("Unhandled exception", ex);
            System.exit(1);
        }
    }
    
    public void shutdown() {
        LOG.info("Shutting down");
        try {
            requestListenerThread.interrupt();
            requestListenerThread.join(5000);
        } catch (InterruptedException ex) {
            LOG.debug(ex);
        }
    }

    public void startup() throws UnknownHostException {
        LOG.info("Starting up");
        requestListenerThread = launch();
        LOG.info("Started");
        try {
            while (true) {
                if (!requestListenerThread.isAlive()) {
                    LOG.info("Listener thread died");
                    requestListenerThread.join();
                    requestListenerThread = launch();
                    LOG.info("Restarted");
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException ex) {
            LOG.debug(ex);
        }
    }

    private void probeServer() throws ServerException {
        LOG.info("Probing ACLS server");
        Request request = new SimpleRequest(RequestType.USE_VIRTUAL);
        Response response = RequestProcessor.serverSendReceive(request, config);
        switch (response.getType()) {
        case USE_VIRTUAL:
            YesNoResponse uv = (YesNoResponse) response;
            if (!uv.isYes()) {
                throw new ServerException(
                        "The ACLS server has the proxy configured as a normal Facility");
            }
            break;
        default:
            LOG.error("Unexpected response for USE_VIRTUAL request: " + response.getType());
            throw new ServerException(
                    "The ACLS server gave an unexpected response to our probe");
        }
    }

    public static void createSampleConfigurationFile() {
        Configuration sampleConfig = new Configuration();
        sampleConfig.setServerHost("aclsHost.example.com");
        sampleConfig.setProxyHost("proxyHost.example.com");
        Map<String, Facility> facilityMap = new TreeMap<String, Facility>();
        sampleConfig.setFacilities(facilityMap);
        Facility f1 = new Facility();
        f1.setAccessName("jim");
        f1.setAccessPassword("secret");
        f1.setFolderName("/trollscope");
        f1.setDriveName("T");
        f1.setFacilityId("F001");
        f1.setFacilityName("Trollscope 2000T");
        f1.setUseFullScreen(true);
        facilityMap.put("192.168.1.1", f1);
        Facility f2 = new Facility();
        f2.setFacilityId("F002");
        f2.setFacilityName("The hatstand in the corner");
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

    private Thread launch() throws UnknownHostException {
        Thread thread = new Thread(new RequestListener(config, config.getProxyPort(),
                config.getProxyHost(),
                new RequestProcessorFactory() {
                    public Runnable createProcessor(Configuration config, Socket s) {
                        return new RequestProcessor(config, s, AclsProxy.this);
                    }
                }));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.debug(ex);
            }
        });
        thread.start();
        return thread;
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

}
