package au.edu.uq.cmm.aclslib.dummy;

import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.server.Configuration;
import au.edu.uq.cmm.aclslib.server.RequestListener;
import au.edu.uq.cmm.aclslib.server.RequestProcessorFactory;


/**
 * A dummy ACLS server for testing the ACL proxy.
 * 
 * @author scrawley
 */
public class AclsDummyServer {
    private static final Logger LOG = Logger.getLogger(AclsDummyServer.class);
    private static Configuration config;   

    public static void main(String[] args) {
        String configFile = null;
        if (args.length > 0) {
            configFile = args[0];
        }
        try {
            config = Configuration.loadConfiguration(configFile);
            if (config == null) {
                LOG.info("Can't read/load proxy configuration file");
                System.exit(2);
            }
            LOG.info("Starting up");
            Thread requestListener = launch();
            LOG.info("Started");
            try {
                while (true) {
                    requestListener = checkAndRelaunch(requestListener);
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ex) {
                LOG.debug(ex);
            }
            LOG.info("Shutting down");
            try {
                requestListener.interrupt();
                requestListener.join(5000);
            } catch (InterruptedException ex) {
                LOG.debug(ex);
            }
            LOG.info("Exitting normally");
            System.exit(0);
        } catch (Throwable ex) {
            LOG.error("Unhandled exception", ex);
            System.exit(1);
        }
    }

    private static Thread checkAndRelaunch(Thread thread) 
            throws InterruptedException, UnknownHostException {
        if (thread.isAlive()) {
            return thread;
        }
        thread.join();
        return launch();
    }

    private static Thread launch() throws UnknownHostException {
        Thread thread = new Thread(new RequestListener(config, config.getServerPort(),
                config.getServerHost(),
                new RequestProcessorFactory() {
            public Runnable createProcessor(Configuration config, Socket s) {
                return new RequestProcessor(config, s);
            }
        }));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.debug(ex);
            }
        });
        thread.start();
        return thread;
    }
}
