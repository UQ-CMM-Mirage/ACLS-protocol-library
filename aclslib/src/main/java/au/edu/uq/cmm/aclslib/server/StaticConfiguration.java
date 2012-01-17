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
public class StaticConfiguration implements Configuration {
    private static final Logger LOG = Logger.getLogger(StaticConfiguration.class);

    private int proxyPort = 1024;
    private int serverPort = 1024;
    private String serverHost;
    private String proxyHost;
    private boolean useProject;
    private String captureDirectory;
    private String baseFileUrl;
    private String feedId;
    private String feedTitle;
    private String feedAuthor;
    private String feedAuthorEmail;
    private String feedUrl;
    private int feedPageSize = 20;

    private Map<String, SimpleFacilityConfigImpl> facilityMap;

    public final int getProxyPort() {
        return proxyPort;
    }

    public final String getServerHost() {
        return serverHost;
    }

    public final int getServerPort() {
        return serverPort;
    }

    public final boolean isUseProject() {
        return useProject;
    }

    public final void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public final void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public final void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public final void setUseProject(boolean useProject) {
        this.useProject = useProject;
    }

    public final String getProxyHost() {
        return proxyHost;
    }

    public final void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public final String getDummyFacility() {
        for (FacilityConfig facility : getFacilities()) {
            if (facility.isDummy()) {
                return facility.getFacilityId();
            }
        }
        throw new IllegalStateException("There are no dummy facilities");
    }

    public String getCaptureDirectory() {
        return captureDirectory;
    }

    public void setCaptureDirectory(String captureDirectory) {
        this.captureDirectory = captureDirectory;
    }

    public String getBaseFileUrl() {
        return baseFileUrl;
    }

    public void setBaseFileUrl(String baseFileUrl) {
        this.baseFileUrl = baseFileUrl;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public String getFeedAuthor() {
        return feedAuthor;
    }

    public void setFeedAuthor(String feedAuthor) {
        this.feedAuthor = feedAuthor;
    }

    public String getFeedAuthorEmail() {
        return feedAuthorEmail;
    }

    public void setFeedAuthorEmail(String feedAuthorEmail) {
        this.feedAuthorEmail = feedAuthorEmail;
    }
    
    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }
    
    public int getFeedPageSize() {
        return feedPageSize ;
    }

    public void setFeedPageSize(int feedPageSize) {
        this.feedPageSize = feedPageSize;
    }

    public Map<String, SimpleFacilityConfigImpl> getFacilityMap() {
        return facilityMap;
    }

    public void setFacilityMap(Map<String, SimpleFacilityConfigImpl> facilityMap) {
        this.facilityMap = facilityMap;
    }

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
                StaticConfiguration res = mapper.readValue(cf, StaticConfiguration.class);
                for (Map.Entry<String, SimpleFacilityConfigImpl> entry : 
                            res.facilityMap.entrySet()) {
                    entry.getValue().setAddress(entry.getKey());
                }
                return res;
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
