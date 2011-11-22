package au.edu.uq.cmm.aclslib.proxy;

import java.net.InetAddress;
import java.util.Map;

public class Configuration {

    private Map<String, Facility> facilityMap;
    private int proxyPort = 1024;
    private int serverPort = 1024;
    private String serverHost;
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

    public Facility lookupFacility(InetAddress addr) {
        Facility facility = facilityMap.get(addr.getHostAddress());
        if (facility == null) {
            facility = facilityMap.get(addr.getHostName());
        }
        return facility;
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

}
