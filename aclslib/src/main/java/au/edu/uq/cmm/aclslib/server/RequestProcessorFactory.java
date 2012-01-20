package au.edu.uq.cmm.aclslib.server;

import java.net.Socket;

import au.edu.uq.cmm.aclslib.config.Configuration;


public interface RequestProcessorFactory {

    Runnable createProcessor(Configuration config, Socket s);

}
