package au.edu.uq.cmm.aclslib.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class RequestReaderTest {

    @Test
    public void testConstructor() {
        new RequestReaderImpl();
    }   
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand() {
        reader().read(source("Z:\n"));
    }
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand2() {
        reader().read(source("1\n"));
    }
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand3() {
        reader().read(source("1?\n"));
    }
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand4() {
        reader().read(source("9999:\n"));
    }
    
    @Test
    public void testReadLogin() {
        RequestReader r = reader();
        Request req = r.read(source("1:steve|secret|"));
        assertEquals(RequestType.LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertNull(login.getFacility());
    }
    
    @Test
    public void testReadStaffLogin() {
        RequestReader r = reader();
        Request req = r.read(source("21:steve|secret|"));
        assertEquals(RequestType.STAFF_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertNull(login.getFacility());
    }
    
    @Test
    public void testReadVirtualLogin() {
        RequestReader r = reader();
        Request req = r.read(source("11:steve|secret|?here|"));
        assertEquals(RequestType.VIRTUAL_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertEquals("here", login.getFacility());
    }
    
    @Test
    public void testReadNewVirtualLogin() {
        RequestReader r = reader();
        Request req = r.read(source("14:steve|secret?|?here|"));
        assertEquals(RequestType.NEW_VIRTUAL_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret?", login.getPassword());
        assertEquals("here", login.getFacility());
    }

    @Test
    public void testReadLogout() {
        RequestReader r = reader();
        Request req = r.read(source("2:steve|acc1|"));
        assertEquals(RequestType.LOGOUT, req.getType());
        LogoutRequest logout = (LogoutRequest) req;
        assertEquals("steve", logout.getUserName());
        assertEquals("acc1", logout.getAccount());
        assertNull(logout.getFacility());
    }

    @Test
    public void testReadVirtualLogout() {
        RequestReader r = reader();
        Request req = r.read(source("12:steve|acc1|?here|"));
        assertEquals(RequestType.VIRTUAL_LOGOUT, req.getType());
        LogoutRequest logout = (LogoutRequest) req;
        assertEquals("steve", logout.getUserName());
        assertEquals("acc1", logout.getAccount());
        assertEquals("here", logout.getFacility());
    }

    @Test
    public void testReadAccount() {
        RequestReader r = reader();
        Request req = r.read(source("3:steve|acc1|"));
        assertEquals(RequestType.ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertNull(acc.getFacility());
    }

    @Test
    public void testReadVirtualAccount() {
        RequestReader r = reader();
        Request req = r.read(source("13:steve|acc1|?here|"));
        assertEquals(RequestType.VIRTUAL_ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertEquals("here", acc.getFacility());
    }

    @Test
    public void testReadNewVirtualAccount() {
        RequestReader r = reader();
        Request req = r.read(source("15:steve|acc1|?here|"));
        assertEquals(RequestType.NEW_VIRTUAL_ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertEquals("here", acc.getFacility());
    }

    @Test
    public void testReadNotes() {
        RequestReader r = reader();
        Request req = r.read(source(
                "4:charlie|unicorn|~Put a banana in your ear|"));
        assertEquals(RequestType.NOTES, req.getType());
        NoteRequest acc = (NoteRequest) req;
        assertEquals("charlie", acc.getUserName());
        assertEquals("unicorn", acc.getAccount());
        assertEquals("Put a banana in your ear", acc.getNotes());
    }

    @Test
    public void testFacility() {
        RequestReader r = reader();
        Request req = r.read(source("5:"));
        assertEquals(RequestType.FACILITY_NAME, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testProject() {
        RequestReader r = reader();
        Request req = r.read(source("6:"));
        assertEquals(RequestType.USE_PROJECT, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testTimer() {
        RequestReader r = reader();
        Request req = r.read(source("7:"));
        assertEquals(RequestType.USE_TIMER, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testFullScreen() {
        RequestReader r = reader();
        Request req = r.read(source("23:"));
        assertEquals(RequestType.USE_FULL_SCREEN, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }

    @Test
    public void testFacilityType() {
        RequestReader r = reader();
        Request req = r.read(source("8:"));
        assertEquals(RequestType.USE_VIRTUAL, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testFacilityCount() {
        RequestReader r = reader();
        Request req = r.read(source("9:"));
        assertEquals(RequestType.FACILITY_COUNT, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testFacilityList() {
        RequestReader r = reader();
        Request req = r.read(source("10:"));
        assertEquals(RequestType.FACILITY_LIST, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testSystemPassword() {
        RequestReader r = reader();
        Request req = r.read(source("20:"));
        assertEquals(RequestType.SYSTEM_PASSWORD, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testNetDrive() {
        RequestReader r = reader();
        Request req = r.read(source("22:"));
        assertEquals(RequestType.NET_DRIVE, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    private RequestReader reader() {
        return new RequestReaderImpl();
    }
    
    private InputStream source(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError("UTF-8??");
        }
    }
}
