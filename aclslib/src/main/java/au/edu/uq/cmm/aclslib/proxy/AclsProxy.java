package au.edu.uq.cmm.aclslib.proxy;

import org.apache.log4j.Logger;


/**
 * 
 */
public class AclsProxy {
    private static final Logger LOG = Logger.getLogger(AclsProxy.class);
    private static Configuration config = new Configuration();

    public static void main(String[] args) {
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
        LOG.info("Exitting");
        System.exit(0);
    }

    private static Thread checkAndRelaunch(Thread thread) throws InterruptedException {
        if (thread.isAlive()) {
            return thread;
        }
        thread.join();
        return launch();
    }

    private static Thread launch() {
        Thread thread = new Thread(new RequestListener(config));
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
