package au.edu.uq.cmm.aclslib.proxy;

import org.apache.log4j.Logger;


/**
 * 
 */
public class AclsProxy {
    private static final Logger LOG = Logger.getLogger(AclsProxy.class);

    public static void main(String[] args) {
        LOG.info("Starting up");
        Thread requestListener = launch(new RequestListener());
        LOG.info("Started");
        try {
            while (true) {
                requestListener = checkAndRelaunch(requestListener, new RequestListener());
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

    private static Thread checkAndRelaunch(Thread thread, Runnable listener) throws InterruptedException {
        if (thread.isAlive()) {
            return thread;
        }
        thread.join();
        return launch(listener);
    }

    private static Thread launch(Runnable listener) {
        Thread thread = new Thread(listener);
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
