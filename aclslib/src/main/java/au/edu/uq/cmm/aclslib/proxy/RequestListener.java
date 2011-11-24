package au.edu.uq.cmm.aclslib.proxy;

import java.io.IOException;
import java.io.InterruptedIOException;
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
            ss = new ServerSocket(config.getProxyPort());
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
