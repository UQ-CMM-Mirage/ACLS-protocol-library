package au.edu.uq.cmm.aclslib.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class represents the configuration details of an ACLS proxy.
 * 
 * @author scrawley
 */
public class StaticConfiguration extends ConfigurationBase implements Configuration {
    private static final Logger LOG = Logger.getLogger(StaticConfiguration.class);

    Map<String, SimpleFacilityConfigImpl> facilityMap;
    
    public Collection<FacilityConfig> getFacilities() {
        return new ArrayList<FacilityConfig>(facilityMap.values());
    }

    public FacilityConfig lookupFacilityByAddress(InetAddress addr) {
        FacilityConfig facility = facilityMap.get(addr.getHostAddress());
        if (facility == null) {
            facility = facilityMap.get(addr.getHostName());
        }
        return facility;
    }

    public FacilityConfig lookupFacilityById(String id) {
        for (FacilityConfig f : facilityMap.values()) {
            if (id.equals(f.getFacilityId())) {
                return f;
            }
        }
        return null;
    }

    public void setFacilities(Map<String, SimpleFacilityConfigImpl> facilityMap) {
        this.facilityMap = facilityMap;
    }

    public static StaticConfiguration loadConfiguration(String configFile) {
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
                return mapper.readValue(cf, StaticConfiguration.class);
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

}
