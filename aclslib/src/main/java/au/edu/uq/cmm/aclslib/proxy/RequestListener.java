package au.edu.uq.cmm.aclslib.proxy;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class RequestListener implements Runnable {
    private static final Logger LOG = Logger.getLogger(RequestListener.class);
    private Configuration config;
    
    public RequestListener(Configuration config) {
        super();
        this.config = config;
    }

    public void run() {
        ServerSocket ss;
        try {
            // FIXME - parameterize the bind address.
            InetAddress bindAddr = InetAddress.getByName("10.33.1.174");
            ss = new ServerSocket(config.getProxyPort(), 5, bindAddr);
            LOG.debug("Proxy listening for requests on " + ss.getInetAddress() + 
                    " port " + ss.getLocalPort());
        } catch (IOException ex) {
            throw new ProxyException("Startup / restart failed", ex);
        }
        while (true) {
            try {
                Socket s = ss.accept();
                // FIXME - Use a bounded thread pool executor.
                new Thread(new RequestProcessor(config, s)).start();
            } catch (InterruptedIOException ex) {
                // FIXME - Synchronously shut down the processor pool.
            } catch (IOException ex) {
                LOG.debug(ex);
            }
        }
    }

}
