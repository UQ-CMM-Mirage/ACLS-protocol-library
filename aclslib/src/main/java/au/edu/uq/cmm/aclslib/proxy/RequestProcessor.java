package au.edu.uq.cmm.aclslib.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.message.CommandErrorResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestReader;
import au.edu.uq.cmm.aclslib.message.RequestReaderImpl;
import au.edu.uq.cmm.aclslib.message.Response;

/**
 * 
 * @author scrawley
 */
public class RequestProcessor implements Runnable {
    private static final Logger LOG = Logger.getLogger(RequestProcessor.class);
    private Socket clientSocket;

    public RequestProcessor(Socket s) {
        this.clientSocket = s;
    }

    public void run() {
        try {
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            RequestReader reader = new RequestReaderImpl();
            Request m = reader.read(is);
            switch (m.getType()) {
            case LOGIN: 
            case LOGOUT: 
            case ACCOUNT:
            case NOTES:
            case FACILITY_NAME:
            case USE_PROJECT: 
            case USE_TIMER: 
            case USE_VIRTUAL: 
            case SYSTEM_PASSWORD: 
            case STAFF_LOGIN:
            case NET_DRIVE: 
            case USE_FULLSCREEN:
                break;
            case FACILITY_COUNT:
            case FACILITY_LIST: 
            case VIRTUAL_LOGIN: 
            case VIRTUAL_LOGOUT: 
            case VIRTUAL_ACCOUNT: 
            case NEW_VIRTUAL_LOGIN: 
            case NEW_VIRTUAL_ACCOUNT:
                sendErrorResponse(os);
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

    private void sendErrorResponse(OutputStream os) {
        sendResponse(os, new CommandErrorResponse());
    }

    private void sendResponse(OutputStream os, Response response) {
        
    }
}
