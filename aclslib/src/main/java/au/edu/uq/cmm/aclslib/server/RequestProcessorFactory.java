package au.edu.uq.cmm.aclslib.server;

import java.net.Socket;
import java.net.UnknownHostException;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;


public interface RequestProcessorFactory {

    Runnable createProcessor(
            ACLSProxyConfiguration config, FacilityMapper facilityMapper, Socket s) 
            throws UnknownHostException;

}
