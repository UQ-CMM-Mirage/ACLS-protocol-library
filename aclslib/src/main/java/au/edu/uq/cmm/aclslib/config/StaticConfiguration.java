/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.config;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;


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
    private boolean allowUnknownClients;
    private Set<String> trustedAddresses;
    private Set<InetAddress> trustedInetAddresses;

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

    public boolean isAllowUnknownClients() {
        return allowUnknownClients;
    }

    public void setAllowUnknownClients(boolean allowUnknownClients) {
        this.allowUnknownClients = allowUnknownClients;
    }

    public Set<String> getTrustedAddresses() {
        return trustedAddresses;
    }

    public void setTrustedAddresses(Set<String> trustedAddresses) 
            throws UnknownHostException {
        this.trustedAddresses = trustedAddresses;
        this.trustedInetAddresses = new HashSet<InetAddress>(trustedAddresses.size());
        for (String address : trustedAddresses) {
            trustedInetAddresses.add(InetAddress.getByName(address));
        }
    }

    @JsonIgnore
    public Set<InetAddress> getTrustedInetAddresses() {
        return trustedInetAddresses;
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
