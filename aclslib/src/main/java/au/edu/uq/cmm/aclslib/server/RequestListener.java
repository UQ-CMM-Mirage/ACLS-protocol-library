package au.edu.uq.cmm.aclslib.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.service.MonitoredThreadServiceBase;
import au.edu.uq.cmm.aclslib.service.ServiceException;

public class RequestListener extends MonitoredThreadServiceBase {
    private static final Logger LOG = 
            LoggerFactory.getLogger(RequestListener.class);
    private ACLSProxyConfiguration config;
    private FacilityMapper mapper;
    private RequestProcessorFactory factory;
    private InetAddress bindAddr;
    private int port;
    private ServerSocket ss;
    
    public RequestListener(ACLSProxyConfiguration config, FacilityMapper mapper, 
            int port, String bindHost, RequestProcessorFactory factory) 
    throws UnknownHostException {
        super();
        this.config = config;
        this.mapper = mapper;
        this.factory = factory;
        this.port = port;
        this.bindAddr = InetAddress.getByName(bindHost);
    }

    public void run() {
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
                new Thread(factory.createProcessor(config, mapper, s)).start();
            } catch (InterruptedIOException ex) {
                // FIXME - Synchronously shut down the processor pool.
                LOG.info("Interrupted - we're done");
                break;
            } catch (IOException ex) {
                if (Thread.currentThread().isInterrupted()) {
                    LOG.info("Interrupted - we're done (2)");
                    break;
                }
                LOG.debug("IO error", ex);
            }
        }
    }
    
    protected void unblock() {
        try {
            LOG.info("Unblocking proxy listener");
            ss.close();
        } catch (IOException ex) {
            LOG.debug("IO error", ex);
        }
    }

}
