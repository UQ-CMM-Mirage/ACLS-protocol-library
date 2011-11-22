package au.edu.uq.cmm.aclslib.proxy;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * 
 */
public class AclsProxy {
    private static final Logger LOG = Logger.getLogger(AclsProxy.class);
    private static Configuration config;   
    private static JsonFactory jf = new JsonFactory();

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--createSampleConfig")) {
            createSampleConfiguration();
            return;
        }
        try {
            loadConfiguration();
            if (config == null) {
                LOG.info("Can't read/load proxy configuration file");
                System.exit(2);
            }
            LOG.info("Starting up");
            Thread requestListener = launch();
            LOG.info("Started");
            try {
                while (true) {
                    requestListener = checkAndRelaunch(requestListener);
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ex) {
                LOG.debug(ex);
            }
            LOG.info("Shutting down");
            try {
                requestListener.interrupt();
                requestListener.join(5000);
            } catch (InterruptedException ex) {
                LOG.debug(ex);
            }
            LOG.info("Exitting normally");
            System.exit(0);
        } catch (Throwable ex) {
            LOG.error("Unhandled exception", ex);
            System.exit(1);
        }
    }
    
    private static void createSampleConfiguration() {
        Configuration sampleConfig = new Configuration();
        sampleConfig.setServerHost("aclsHost.example.com");
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

    private static void loadConfiguration() {
        // Load configuration from a JSON file.
        try {
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readValue(new File("config.json"), Configuration.class);
        } catch (JsonParseException e) {
            LOG.error(e);
        } catch (JsonMappingException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static Thread checkAndRelaunch(Thread thread) throws InterruptedException {
        if (thread.isAlive()) {
            return thread;
        }
        thread.join();
        return launch();
    }

    private static Thread launch() {
        Thread thread = new Thread(new RequestListener(config));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.debug(ex);
            }
        });
        thread.start();
        return thread;
    }
}
