package au.edu.uq.cmm.aclslib.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the configuration details of an ACLS proxy backed by a
 * JSON file.
 * 
 * @author scrawley
 */
public class StaticConfiguration implements Configuration {
    private static final Logger LOG = 
            LoggerFactory.getLogger(StaticConfiguration.class);

    private int proxyPort = 1024;
    private int serverPort = 1024;
    private String serverHost;
    private String proxyHost;
    private boolean useProject;
    private String captureDirectory;
    private String archiveDirectory;
    private String baseFileUrl;
    private String feedId;
    private String feedTitle;
    private String feedAuthor;
    private String feedAuthorEmail;
    private String feedUrl;
    private int feedPageSize = 20;
    private long queueExpiryTime;
    private long queueExpiryInterval;
    private boolean expireByDeleting;
    private DataGrabberRestartPolicy dataGrabberRestartPolicy = 
            DataGrabberRestartPolicy.NO_AUTO_START;
    private boolean holdDatasetsWithNoUser = true;
    private String primaryRepositoryUrl;
    private String aclsUrl;
    
    private Map<String, StaticFacilityConfig> facilityMap;

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
        for (FacilityConfig facility : getFacilityConfigs()) {
            if (facility.isDummy()) {
                return facility.getFacilityName();
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

    public String getArchiveDirectory() {
        return archiveDirectory;
    }

    public void setArchiveDirectory(String archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
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

    public Map<String, StaticFacilityConfig> getFacilityMap() {
        return facilityMap;
    }

    public long getQueueExpiryTime() {
        return queueExpiryTime;
    }

    public void setQueueExpiryTime(long queueExpiryTime) {
        this.queueExpiryTime = queueExpiryTime;
    }

    public long getQueueExpiryInterval() {
        return queueExpiryInterval;
    }

    public void setQueueExpiryInterval(long queueExpiryInterval) {
        this.queueExpiryInterval = queueExpiryInterval;
    }

    public boolean isExpireByDeleting() {
        return expireByDeleting;
    }

    public void setExpireByDeleting(boolean expireByDeleting) {
        this.expireByDeleting = expireByDeleting;
    }

    public DataGrabberRestartPolicy getDataGrabberRestartPolicy() {
        return dataGrabberRestartPolicy;
    }

    public void setDataGrabberRestartPolicy(
            DataGrabberRestartPolicy dataGrabberRestartPolicy) {
        this.dataGrabberRestartPolicy = dataGrabberRestartPolicy;
    }

    public void setFacilityMap(Map<String, StaticFacilityConfig> facilityMap) {
        this.facilityMap = facilityMap;
    }

    public String getPrimaryRepositoryUrl() {
        return primaryRepositoryUrl;
    }

    public void setPrimaryRepositoryUrl(String primaryRepositoryUrl) {
        this.primaryRepositoryUrl = primaryRepositoryUrl;
    }

    public String getAclsUrl() {
        return aclsUrl;
    }

    public void setAclsUrl(String aclsUrl) {
        this.aclsUrl = aclsUrl;
    }

    public Collection<FacilityConfig> getFacilityConfigs() {
        return new ArrayList<FacilityConfig>(facilityMap.values());
    }

    public FacilityConfig lookupFacilityByAddress(InetAddress addr) {
        FacilityConfig facility = facilityMap.get(addr.getHostAddress());
        if (facility == null) {
            facility = facilityMap.get(addr.getHostName());
        }
        return facility;
    }

    public boolean isHoldDatasetsWithNoUser() {
        return holdDatasetsWithNoUser;
    }

    public void setHoldDatasetsWithNoUser(boolean holdDatasetsWithNoUser) {
        this.holdDatasetsWithNoUser = holdDatasetsWithNoUser;
    }

    public FacilityConfig lookupFacilityByName(String name) {
        for (FacilityConfig f : facilityMap.values()) {
            if (name.equals(f.getFacilityName())) {
                return f;
            }
        }
        return null;
    }

    public void setFacilities(Map<String, StaticFacilityConfig> facilityMap) {
        this.facilityMap = facilityMap;
    }
    
    public FacilityConfig lookupFacilityByLocalHostId(String localHostId) {
        for (FacilityConfig f : facilityMap.values()) {
            if (localHostId.equals(f.getLocalHostId())) {
                return f;
            }
        }
        return null;
    }

    /**
     * Read the configuration from an input stream
     * 
     * @param configFile
     * @return the configuration or null if there was a problem reading.
     * @throws ConfigurationException 
     */
    public static StaticConfiguration readConfiguration(InputStream is)
            throws ConfigurationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            StaticConfiguration res = mapper.readValue(is, StaticConfiguration.class);
            for (Map.Entry<String, StaticFacilityConfig> entry : 
                    res.facilityMap.entrySet()) {
                entry.getValue().setAddress(entry.getKey());
            }
            return res;
        } catch (JsonParseException ex) {
            throw new ConfigurationException("The configuration is not valid JSON", ex);
        } catch (JsonMappingException ex) {
            throw new ConfigurationException("The configuration JSON doesn't match " +
                    "the StaticConfiguration class", ex);
        } catch (IOException ex) {
            throw new ConfigurationException(
                    "IO error while reading the configuration", ex);
        } 
    }

    /**
     * Load the configuration from a file.
     * 
     * @param configFile
     * @return the configuration or null if it couldn't be found / read.
     * @throws ConfigurationException 
     */
    public static StaticConfiguration loadConfiguration(String configFile) 
            throws ConfigurationException {
        InputStream is = null;
        try {
            File cf = new File(configFile == null ? "config.json" : configFile);
                is = new FileInputStream(cf);
                return readConfiguration(is);
        } catch (IOException ex) {
            throw new ConfigurationException("Cannot open file '" + configFile + "'", ex);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * Load the configuration from a URL.  This understands any URL that the
     * JVM has a protocol handler for, and also "classpath:" URLs. 
     * @return the configuration or null
     * @param urlString the URL for the config file
     * @throws URISyntaxException 
     * @throws MalformedURLException 
     */
    public static StaticConfiguration loadConfigurationFromUrl(String urlString) 
            throws ConfigurationException {
        InputStream is = null;
        try {
            URI uri = new URI(urlString);
            if (uri.getScheme().equals("classpath")) {
                String path = uri.getSchemeSpecificPart();
                LOG.debug("Loading configuration from classpath resource '" +
                        path + "'");
                is = StaticConfiguration.class.getClassLoader().getResourceAsStream(path);
                if (is == null) {
                    throw new IllegalArgumentException("Cannot locate resource '" +
                            path + "' on the classpath");
                }
            } else {
                LOG.debug("Loading configuration from url '" + urlString + "'");
                is = uri.toURL().openStream();
            }
            return readConfiguration(is);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(
                    "Invalid urlString '" + urlString + "'", ex);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(
                    "Invalid urlString '" + urlString + "'", ex);
        } catch (IOException ex) {
            throw new ConfigurationException("Cannot open URL input stream", ex);
        } finally {
            closeQuietly(is);
        }
    }
    
    private static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            // ignore
        }
    }
}
