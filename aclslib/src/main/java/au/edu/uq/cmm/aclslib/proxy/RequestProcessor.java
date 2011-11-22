package au.edu.uq.cmm.aclslib.proxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.message.AbstractMessage;
import au.edu.uq.cmm.aclslib.message.CommandErrorResponse;
import au.edu.uq.cmm.aclslib.message.LoginRequest;
import au.edu.uq.cmm.aclslib.message.LoginResponse;
import au.edu.uq.cmm.aclslib.message.NetDriveResponse;
import au.edu.uq.cmm.aclslib.message.ProxyErrorResponse;
import au.edu.uq.cmm.aclslib.message.RefusedResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestReader;
import au.edu.uq.cmm.aclslib.message.RequestReaderImpl;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.ResponseReaderImpl;
import au.edu.uq.cmm.aclslib.message.ResponseType;
import au.edu.uq.cmm.aclslib.message.ServerStatusException;
import au.edu.uq.cmm.aclslib.message.YesNoResponse;

/**
 * 
 * @author scrawley
 */
public class RequestProcessor implements Runnable {
    private static final Logger LOG = Logger.getLogger(RequestProcessor.class);
    private Socket clientSocket;
    private Configuration config;

    public RequestProcessor(Configuration config, Socket s) {
        this.clientSocket = s;
        this.config = config;
    }

    public void run() {
        try {
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                    clientSocket.getOutputStream()));
            InetAddress addr = clientSocket.getInetAddress();
            Facility f = config.lookupFacility(addr);
            if (f == null) {
                w.append("Proxy has no facility details for " + addr + "\r\n").flush();
                return;
            }
            InputStream is = clientSocket.getInputStream();
            RequestReader reader = new RequestReaderImpl();
            Request m = reader.read(is);
            
            switch (m.getType()) {
            case LOGIN:
                processLoginRequest(f, m, w);
                break;
            case LOGOUT: 
                processLogoutRequest(f, m, w);
                break;
            case ACCOUNT:
                processAccountRequest(f, m, w);
                break;
            case NOTES:
                processNotesRequest(f, m, w);
                break;
            case FACILITY_NAME:
                processFacilityRequest(f, m, w);
                break;
            case USE_PROJECT: 
                processUseProjectRequest(f, m, w);
                break;
            case USE_TIMER: 
                processUseTimerRequest(f, m, w);
                break;
            case USE_VIRTUAL: 
                processUseVirtualRequest(f, m, w);
                break;
            case SYSTEM_PASSWORD: 
                processSystemPasswordRequest(f, m, w);
                break;
            case STAFF_LOGIN:
                processStaffLoginRequest(f, m, w);
                break;
            case NET_DRIVE: 
                processNetDriveRequest(f, m, w);
                break;
            case USE_FULLSCREEN:
                processUseFullscreenRequest(f, m, w);
                break;
            default:
                // We have told the client that we don't support the virtual
                // extensions, so it is an error for it to send them to us.
                // Anything else ... is an extension we don't understand.
                sendErrorResponse(w);
                break;
            }
        } catch (IOException ex) {
            LOG.error(ex);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // ignore this.
            }
        }
    }

    private void processUseFullscreenRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        Response r = new YesNoResponse(f.isUserFullscreen() ? 
                ResponseType.FULLSCREEN_YES : ResponseType.FULLSCREEN_NO);
        sendResponse(w, r);
    }

    private void processNetDriveRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        Response r;
        if (f.isUseNetDrive()) {
            r = new NetDriveResponse(f.getDriveName(), f.getFolderName(),
                    f.getAccessName(), f.getAccessPassword());
        } else {
            r = new NetDriveResponse();
        }
        sendResponse(w, r);
    }

    private void processStaffLoginRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processSystemPasswordRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processUseVirtualRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        Response r = new YesNoResponse(ResponseType.USE_VIRTUAL, false);
        sendResponse(w, r);
    }

    private void processUseTimerRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processUseProjectRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processFacilityRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processNotesRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processAccountRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processLogoutRequest(Facility f, Request m, BufferedWriter w) {
        // TODO Auto-generated method stub
        
    }

    private void processLoginRequest(Facility f, Request m, BufferedWriter w) 
            throws IOException {
        LoginRequest l = (LoginRequest) m;
        Request vl = new LoginRequest(RequestType.VIRTUAL_LOGIN, 
                l.getUserName(), l.getPassword(), f.getFacilityId());
        {
            Response r;
            Response vr = serverSendReceive(vl);
            switch (vr.getType()) {
            case VIRTUAL_LOGIN_ALLOWED:
                LoginResponse vlr = (LoginResponse) vr;
                r = new LoginResponse(ResponseType.LOGIN_ALLOWED, 
                        vlr.getUserName(), vlr.getOrgName(), 
                        vlr.getAccounts(), vlr.getCertification(), vlr.isOnsiteAssist());
                break;
            case VIRTUAL_LOGIN_REFUSED:
                r = new RefusedResponse(ResponseType.LOGIN_REFUSED);
                break;
            case ERROR:
                r = vr;
                break;
            default:
                LOG.error("Unexpected response for virtual login");
                r = new ProxyErrorResponse("Proxy got unexpected response from ACLS server");
            }
            sendResponse(w, r);
        }
    }

    private void sendErrorResponse(BufferedWriter w) throws IOException {
        sendResponse(w, new CommandErrorResponse());
    }

    private Response serverSendReceive(Request request) {
        try {
            Socket aclsSocket = new Socket(config.getServerHost(), config.getServerPort());
            try {
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        aclsSocket.getOutputStream()));
                InputStream is = aclsSocket.getInputStream();
                sendRequest(w, request);
                return new ResponseReaderImpl().readWithStatusLine(is);
            } catch (ServerStatusException ex) {
                LOG.error("ACLS server refused request: " + ex);
                return new ProxyErrorResponse("Proxy got status error from ACLS server");
            } finally {
                aclsSocket.close();
            }
        } catch (IOException ex) {
            LOG.warn("IO error while trying to talk to ACLS server", ex);
            return new ProxyErrorResponse("Proxy cannot talk to ACLS server");
        }
    }

    private void sendResponse(BufferedWriter w, Response response) throws IOException {
        if (response instanceof ProxyErrorResponse) {
            w.append(((ProxyErrorResponse) response).getMessage() + "\r\n");
        } else {
            w.append(AbstractMessage.ACCEPTED_IP_TAG + "\r\n");
            w.append(response.unparse() + "\r\n").flush();
        }
    }
    
    private void sendRequest(BufferedWriter w, Request request) throws IOException {
        w.append(request.unparse() + "\r\n").flush();
    }
}
