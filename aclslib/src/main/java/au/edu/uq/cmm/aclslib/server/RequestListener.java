package au.edu.uq.cmm.aclslib.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.service.MonitoredThreadServiceBase;
import au.edu.uq.cmm.aclslib.service.ServiceException;


public class RequestListener extends MonitoredThreadServiceBase {
    private static final Logger LOG = Logger.getLogger(RequestListener.class);
    private Configuration config;
    private RequestProcessorFactory factory;
    private InetAddress bindAddr;
    private int port;
    
    public RequestListener(Configuration config, int port, String bindHost,
            RequestProcessorFactory factory) throws UnknownHostException {
        super();
        this.config = config;
        this.factory = factory;
        this.port = port;
        this.bindAddr = InetAddress.getByName(bindHost);
    }

    public void run() {
        ServerSocket ss;
        try {
            LOG.debug("Starting proxy listener for address = '" + bindAddr +
                    "', port = " + port);
            ss = new ServerSocket(port, 5, bindAddr);
            LOG.debug("Proxy is listening for requests on " + ss.getInetAddress() + 
                    " port " + ss.getLocalPort());
        } catch (IOException ex) {
            LOG.error("Error while creating / binding the proxy's server socket", ex);
            throw new ServiceException("Startup / restart failed", ex);
        }
        while (true) {
            try {
                Socket s = ss.accept();
                // FIXME - Use a bounded thread pool executor.
                new Thread(factory.createProcessor(config, s)).start();
            } catch (InterruptedIOException ex) {
                // FIXME - Synchronously shut down the processor pool.
            } catch (IOException ex) {
                LOG.debug(ex);
            }
        }
    }

}
