package au.edu.uq.cmm.aclslib.proxy;

import java.net.InetAddress;
import java.util.Map;

public class Configuration {
    private Map<InetAddress, Facility> facilityMap;
    private int proxyPort = 1024;
    private int serverPort = 1024;

    public Facility lookupFacility(InetAddress addr) {
        return facilityMap.get(addr);
    }

    public int getPortProxyPort() {
        return proxyPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerHost() {
        // TODO Auto-generated method stub
        return null;
    }

}
