package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import au.edu.uq.cmm.aclslib.message.AbstractMessage;
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
            proxy.registerListener(new AclsFacilityEventListener() {
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

    private void registerListener(AclsFacilityEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void shutdown() {
        LOG.info("Shutting down");
        try {
            requestListenerThread.interrupt();
            requestListenerThread.join(5000);
        } catch (InterruptedException ex) {
            LOG.debug(ex);
        }
    }

    private void startup() throws UnknownHostException {
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
        try {
            Socket aclsSocket = new Socket(config.getServerHost(), config.getServerPort());
            try {
                InputStream is = aclsSocket.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String statusLine = r.readLine();
                if (statusLine == null) {
                    throw new ServerException("ACLS server closed connection");
                } else if (!statusLine.equals(AbstractMessage.ACCEPTED_IP_TAG)) {
                    throw new ServerException("ACLS server status - " + statusLine);
                }
            } finally {
                aclsSocket.close();
            }
        }
        catch (IOException ex) {
            throw new ServerException("IO error while probing ACLS server", ex);
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
}
