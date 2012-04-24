package au.edu.uq.cmm.aclslib.config;

import java.net.InetAddress;
import java.util.Set;


/**
 * The combined configuration property API for the ACLSProxy.
 * Different implementations support different persistence mechanisms.
 * 
 * @author scrawley
 */
public interface ACLSProxyConfiguration {
    
    /**
     * Get the ACLS proxy's port number
     */
    int getProxyPort();

    /**
     * Get the ACLS server's hostname or IP address
     */
    String getServerHost();

    /**
     * Get the ACLS server's port number
     */
    int getServerPort();

    /**
     * Get the 'useProject' flag.  This determines whether ACLS clients 
     * describe the user's ACLS account as an "account" or "project" name.
     */
    boolean isUseProject();

    /**
     * Get the hostname or IP address of the ACLSProxy.
     */
    String getProxyHost();

    /**
     * Get the name of the "dummy facility" that is used for checking
     * ACLS user names and passwords.
     * 
     * @return the facility name or null.
     */
    String getDummyFacilityName();

    /**
     * Get the hostId string for the "dummy facility".
     * 
     * @return the host id or null.
     */
    String getDummyFacilityHostId();

    /**
     * Get the flag that says whether we accept requests from address that
     * we don't know about.
     * 
     * @return the flag
     */
    boolean isAllowUnknownClients();

    /**
     * Get the set of addresses that we trust ... in addition to those of
     * the configured facilities.
     * 
     * @return the trusted addresses
     */
    Set<String> getTrustedAddresses();

    /**
     * Same as {@link #getTrustedAddresses()} but converted {@link InetAddress}
     * instances.
     * 
     * @return the trusted addresses
     */
    Set<InetAddress> getTrustedInetAddresses();
}