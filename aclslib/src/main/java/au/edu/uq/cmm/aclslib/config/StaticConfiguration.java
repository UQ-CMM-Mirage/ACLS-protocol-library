package au.edu.uq.cmm.aclslib.config;

import java.net.MalformedURLException;
import java.net.URISyntaxException;


/**
 * This class represents the configuration details of an ACLS proxy backed by a
 * JSON file.
 * 
 * @author scrawley
 */
public class StaticConfiguration implements ACLSProxyConfiguration {

    private int proxyPort = 1024;
    private int serverPort = 1024;
    private String serverHost;
    private String proxyHost;
    private boolean useProject;

    private String dummyFacilityName;
    private String dummyFacilityHostId;

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

    public final String getDummyFacilityName() {
        return this.dummyFacilityName;
    }

    public String getDummyFacilityHostId() {
        return dummyFacilityHostId;
    }

    public void setDummyFacilityHostId(String dummyFacilityHostId) {
        this.dummyFacilityHostId = dummyFacilityHostId;
    }

    public void setDummyFacilityName(String dummyFacilityName) {
        this.dummyFacilityName = dummyFacilityName;
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
        return new JsonConfigLoader<StaticConfiguration>(StaticConfiguration.class).
                loadConfiguration(configFile);
    }

    /**
     * Load the configuration from a URL.  This understands any URL that the
     * JVM has a protocol handler for, and also "classpath:" URLs. 
     * 
     * @return the configuration or null
     * @param urlString the URL for the config file
     * @throws URISyntaxException 
     * @throws MalformedURLException 
     */
    public static StaticConfiguration loadConfigurationFromUrl(String urlString) 
            throws ConfigurationException {
        return new JsonConfigLoader<StaticConfiguration>(StaticConfiguration.class).
                loadConfigurationFromUrl(urlString);
    }
}
