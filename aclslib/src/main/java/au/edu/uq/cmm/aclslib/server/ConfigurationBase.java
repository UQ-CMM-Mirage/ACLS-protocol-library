package au.edu.uq.cmm.aclslib.server;

public abstract class ConfigurationBase implements Configuration {

    private int proxyPort = 1024;
    private int serverPort = 1024;
    private String serverHost;
    private String proxyHost;
    private boolean useProject;

    public ConfigurationBase() {
        super();
    }

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

}