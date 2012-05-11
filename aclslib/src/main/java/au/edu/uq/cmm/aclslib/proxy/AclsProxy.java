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

package au.edu.uq.cmm.aclslib.proxy;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.authenticator.Authenticator;
import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ServerStatusException;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.server.RequestListener;
import au.edu.uq.cmm.aclslib.server.RequestProcessorFactory;
import au.edu.uq.cmm.aclslib.service.CompositeServiceBase;
import au.edu.uq.cmm.aclslib.service.Service;
import au.edu.uq.cmm.aclslib.service.ServiceException;


/**
 * @author scrawley
 */
public class AclsProxy extends CompositeServiceBase {
    private static final Logger LOG = LoggerFactory.getLogger(AclsProxy.class);
    
    private final ACLSProxyConfiguration config;
    private final Service requestListener;
    private final List<AclsFacilityEventListener> listeners = 
            new ArrayList<AclsFacilityEventListener>();
    // The virtual logout requests requires a password (!?!), so we've 
    // got no choice but to remember it.
    // FIXME - this will need to be persisted if sessions are to survive 
    // beyond a restart.
    private final Map<String, String> passwordCache = new HashMap<String, String>();
    private final Authenticator fallbackAuthenticator;
    private final boolean useVmfl;
    private final int timeout;
  

    public AclsProxy(ACLSProxyConfiguration config, int timeout, 
            FacilityMapper mapper, Authenticator fallbackAuthenticator) {
        this.config = config;
        this.timeout = timeout;
        try {
            this.useVmfl = new AclsClient(config.getServerHost(),
                    config.getServerPort(), timeout).checkForVmflSupport();
            this.requestListener = new RequestListener(config, mapper,
                    config.getProxyPort(), config.getProxyHost(),
                    new RequestProcessorFactory() {
                public Runnable createProcessor(ACLSProxyConfiguration config, FacilityMapper mapper, Socket s) {
                    if (useVmfl) {
                        return new VmflRequestProcessor(config, mapper, s, AclsProxy.this);
                    } else {
                        return new NormalRequestProcessor(config, mapper, s, AclsProxy.this);
                    }
                }
            });
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("Configuration problem", ex);
        }
        this.fallbackAuthenticator = fallbackAuthenticator;
    }

    @Override
    protected void doShutdown() throws ServiceException, InterruptedException {
        LOG.info("Shutting down");
        requestListener.shutdown();
        LOG.info("Shutdown completed");
    }

    @Override
    protected void doStartup() throws ServiceException, InterruptedException {
        LOG.info("Starting up");
        requestListener.startup();        
        LOG.info("Startup completed");
    }

    public Authenticator getFallbackAuthenticator() {
        return fallbackAuthenticator;
    }

    public void probeServer() throws ServiceException {
        LOG.info("Probing ACLS server");
        AclsClient client = new AclsClient(
                config.getServerHost(), config.getServerPort(), timeout);
        try {
            Request request = new SimpleRequest(RequestType.USE_PROJECT, null, null, null);
            Response response = client.serverSendReceive(request);
            switch (response.getType()) {
            case PROJECT_NO:
            case PROJECT_YES:
                break;
            default:
                LOG.error("Unexpected response for USE_PROJECT request: " + 
                        response.getType());
                throw new ServiceException(
                        "The ACLS server gave an unexpected response " +
                        "to our probe (see log)");
            }
        } catch (AclsCommsException ex) {
            throw new ServiceException(
                    "The ACLS server is not responding", ex);
        } catch (ServerStatusException ex) {
            throw new ServiceException(
                    "The ACLS server rejected our probe", ex);
        } catch (AclsException ex) {
            throw new ServiceException(
                    "The ACLS server is not behaving correctly", ex);
        } 
    }

    public void sendEvent(AclsFacilityEvent event) {
        synchronized (listeners) {
            for (AclsFacilityEventListener listener : listeners) {
                listener.eventOccurred(event);
            }
        }
    }

    public void addListener(AclsFacilityEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(AclsFacilityEventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public Map<String, String> getPasswordCache() {
        return passwordCache;
    }

    public int getTimeout() {
        return timeout;
    }
}
