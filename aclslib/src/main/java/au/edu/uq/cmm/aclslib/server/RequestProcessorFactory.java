package au.edu.uq.cmm.aclslib.server;

import java.net.Socket;


public interface RequestProcessorFactory {

    Runnable createProcessor(Configuration config, Socket s);

}
