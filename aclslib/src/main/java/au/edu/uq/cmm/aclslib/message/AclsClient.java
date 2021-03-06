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

package au.edu.uq.cmm.aclslib.message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple low-level ACLS client class that handles a request / response
 * interaction between ourselves and an ACLS server (or proxy).
 * 
 * @author scrawley
 */
public class AclsClient {
    public static final int ACLS_REQUEST_TIMEOUT = 5000;
    
    private static final Logger LOG = LoggerFactory.getLogger(AclsClient.class);
    private final String serverHost;
    private final int serverPort;
    private int timeout;
    
    
    public AclsClient(String serverHost, int serverPort, int timeout) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.timeout = timeout > 0 ? timeout : ACLS_REQUEST_TIMEOUT;
    }
    
    public Response serverSendReceive(Request r) throws AclsException {
        try {
            Socket aclsSocket = new Socket();
            try {
                aclsSocket.setSoTimeout(timeout);
                aclsSocket.connect(
                        new InetSocketAddress(serverHost, serverPort), timeout);
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        aclsSocket.getOutputStream()));
                InputStream is = aclsSocket.getInputStream();
                LOG.debug("Sending ACLS server request " + r.getType().name() +
                        "(" + r.unparse(true) + ")");
                w.append(r.unparse(false) + "\r\n").flush();
                return new ResponseReaderImpl().readWithStatusLine(is);
            } finally {
                aclsSocket.close();
            }
        } catch (SocketTimeoutException ex) {
            LOG.info("ACLS send / receive timed out");
            throw new AclsNoResponseException(
                    "Timeout while connecting or talking to ACLS server (" +
                    serverHost + ":" + serverPort + ")", ex);
        } catch (IOException ex) {
            LOG.info("ACLS send / receive gave IO exception: " + ex.getMessage());
            throw new AclsCommsException("IO error while trying to talk to ACLS server (" +
                    serverHost + ":" + serverPort + ")", ex);
        }
    }

    public boolean checkForVmflSupport() {
        LOG.debug("Checking for vMFL capability");
        try {
            Request request = new SimpleRequest(RequestType.USE_VIRTUAL, null, null, null);
            Response response = serverSendReceive(request);
            switch (response.getType()) {
            case USE_VIRTUAL:
                YesNoResponse uv = (YesNoResponse) response;
                LOG.info("The 'useVirtual' request returned " + uv.isYes());
                return uv.isYes();
            default:
                throw new AclsProtocolException(
                        "Unexpected response to USE_VIRTUAL request - " + 
                                response.getType());
            }
        } catch (AclsNoResponseException ex) {
            LOG.debug("No response to 'useVirtual' request - assume no vMFL");
            return false;
        } catch (AclsException ex) {
            // We do this in case we are talking to a server that is not
            // aware of the vMFL requests.
            LOG.info("The 'useVirtual' request failed - assuming no vMFL");
            LOG.debug("This is the vMFL check failure cause", ex);
            return false;
        }
    }
}