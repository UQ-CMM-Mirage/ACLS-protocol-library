/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.dummy;

import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.config.StaticConfiguration;
import au.edu.uq.cmm.aclslib.server.RequestListener;
import au.edu.uq.cmm.aclslib.server.RequestProcessorFactory;


/**
 * A dummy ACLS server for testing the ACL proxy.
 * 
 * @author scrawley
 */
public class AclsDummyServer {
    private static final Logger LOG = 
            LoggerFactory.getLogger(AclsDummyServer.class);
    private static StaticConfiguration config;   
    private static FacilityMapper mapper;   

    public static void main(String[] args) {
        String configFile = null;
        if (args.length > 0) {
            configFile = args[0];
        }
        try {
            config = StaticConfiguration.loadConfiguration(configFile);
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
                LOG.debug("interrupted", ex);
            }
            LOG.info("Shutting down");
            try {
                requestListener.interrupt();
                requestListener.join(5000);
            } catch (InterruptedException ex) {
                LOG.debug("interrupted", ex);
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
        Thread thread = new Thread(new RequestListener(config, mapper,
                config.getServerPort(), config.getServerHost(),
                new RequestProcessorFactory() {
            public Runnable createProcessor(
                    ACLSProxyConfiguration config, FacilityMapper mapper, Socket s) {
                return new RequestProcessor(config, mapper, s);
            }
        }));
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable ex) {
                LOG.debug("uncaught", ex);
            }
        });
        thread.start();
        return thread;
    }
}
