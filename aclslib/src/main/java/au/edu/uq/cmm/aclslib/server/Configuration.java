package au.edu.uq.cmm.aclslib.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class Configuration {
    private static final Logger LOG = Logger.getLogger(Configuration.class);

    private Map<String, Facility> facilityMap;
    private int proxyPort = 1024;
    private int serverPort = 1024;
    private String serverHost;
    private String proxyHost;
    private boolean useProject;
    

    public Map<String, Facility> getFacilities() {
        return facilityMap;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public boolean isUseProject() {
        return useProject;
    }

    public Facility lookupFacilityByAddress(InetAddress addr) {
        Facility facility = facilityMap.get(addr.getHostAddress());
        if (facility == null) {
            facility = facilityMap.get(addr.getHostName());
        }
        return facility;
    }

    public Facility lookupFacilityById(String id) {
        for (Facility f : facilityMap.values()) {
            if (id.equals(f.getFacilityId())) {
                return f;
            }
        }
        return null;
    }

    public void setFacilities(Map<String, Facility> facilityMap) {
        this.facilityMap = facilityMap;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setUseProject(boolean useProject) {
        this.useProject = useProject;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    
    public static Configuration loadConfiguration(String configFile) {
        // Load configuration from a JSON file.
        try {
            ObjectMapper mapper = new ObjectMapper();
            File cf = new File(configFile == null ? "config.json" : configFile);
            if (!cf.exists()) {
                LOG.error("Configuration file '" + cf + "' not found");
            } else if (!cf.isFile()) {
                LOG.error("Configuration file '" + cf + "' is not a regular file");
            } else if (!cf.canRead()) {
                LOG.error("Configuration file '" + cf + "' is not readable");
            } else {
                return mapper.readValue(cf, Configuration.class);
            }
        } catch (JsonParseException e) {
            LOG.error(e);
        } catch (JsonMappingException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        }
        return null;
    }

    public String getDummyFacility() {
        for (Facility facility : facilityMap.values()) {
            if (facility.isDummy()) {
                return facility.getFacilityId();
            }
        }
        throw new IllegalStateException("There are no dummy facilities");
    }

}
