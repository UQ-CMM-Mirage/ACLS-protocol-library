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

package au.edu.uq.cmm.aclslib.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.uq.cmm.aclslib.config.ACLSProxyConfiguration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.message.AbstractMessage;
import au.edu.uq.cmm.aclslib.message.AclsCommsException;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.CommandErrorResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestReader;
import au.edu.uq.cmm.aclslib.message.RequestReaderImpl;
import au.edu.uq.cmm.aclslib.message.Response;

public abstract class RequestProcessorBase  implements Runnable {

    private final Socket socket;
    private final ACLSProxyConfiguration config;
    private final FacilityMapper facilityMapper;
    private final Logger logger;
    private final Set<InetAddress> trustedAddresses;
    private final boolean allowUnknownClients;

    public RequestProcessorBase(ACLSProxyConfiguration config, FacilityMapper facilityMapper, 
            Logger logger, Socket socket) {
        super();
        this.socket = socket;
        this.config = config;
        this.facilityMapper = facilityMapper;
        this.logger = logger;
        this.trustedAddresses = config.getTrustedInetAddresses();
        this.allowUnknownClients = config.isAllowUnknownClients();
    }

    protected Logger getLogger() {
        return logger;
    }

    protected void sendErrorResponse(BufferedWriter w) throws AclsCommsException {
        sendResponse(w, new CommandErrorResponse());
    }

    protected void sendResponse(BufferedWriter w, Response r) 
        throws AclsCommsException {
        try {
            logger.debug("Sending response " + r.getType().name() + "(" + r.unparse(true) + ")");
            w.append(r.unparse(false) + "\r\n").flush();
        } catch (IOException ex) {
            throw new AclsCommsException("Couldn't write message", ex);
        }
    }

    protected static void sendRequest(BufferedWriter w, Request r) 
            throws IOException {
        LoggerFactory.getLogger(RequestProcessorBase.class).debug(
                "Sending request " + r.getType().name() + "(" + r.unparse(true) + ")");
        w.append(r.unparse(false) + "\r\n").flush();
    }

    public void run() {
        try {
            InetAddress addr = socket.getInetAddress();
            logger.debug("Processing request from " + addr);
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            if (!trustedAddresses.contains(addr)) {
                // Figure out which 'facility' is talking to us, based on the client
                // IP address.  If the IP address is not known to us, we can't map it
                // to a virtual facility id to log the user in ... so the only sensible
                // thing to do is send a status error.
                FacilityConfig f = facilityMapper.lookup(null, null, addr);
                if (f == null) {
                    logger.debug("Unknown facility: IP is " + addr);
                    if (!allowUnknownClients) {
                        w.append("Proxy has no facility details for " + addr + "\r\n").flush();
                        return;
                    }
                }
            }
            w.append(AbstractMessage.ACCEPTED_IP_TAG + "\r\n").flush();
            
            // Now read the request ...
            InputStream is = socket.getInputStream();
            RequestReader reader = new RequestReaderImpl(facilityMapper, socket.getInetAddress());
            Request m = reader.read(is);
            // ... and dispatch to a "process" method bases on the request type.
            // These methods will deal with the server interaction (if required)
            // and create and return the relevant response.
            doProcess(m, w);
        } catch (IOException ex) {
            logger.error("IO error", ex);
        } catch (AclsException ex) {
            logger.error("ACLS error", ex);
        } catch (Throwable ex) {
            logger.error("Unexpected error", ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                // ignore this.
            }
        }
    }

    protected abstract void doProcess(Request m, BufferedWriter w) throws AclsException;

    public ACLSProxyConfiguration getConfig() {
        return config;
    }
}