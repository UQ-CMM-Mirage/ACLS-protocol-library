package au.edu.uq.cmm.aclslib.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.Test;

import au.edu.uq.cmm.aclslib.config.FacilityMapper;
import au.edu.uq.cmm.aclslib.config.StaticFacilityConfig;

public class RequestReaderTest {

    @Test
    public void testConstructor() {
        new RequestReaderImpl(mapper(), localHost());
    }   
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand() throws AclsException {
        reader().read(source("Z:\n"));
    }
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand2() throws AclsException {
        reader().read(source("1\n"));
    }
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand3() throws AclsException {
        reader().read(source("1?\n"));
    }
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand4() throws AclsException {
        reader().read(source("9999:\n"));
    }
    
    @Test
    public void testReadLogin() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("1:steve|secret|"));
        assertEquals(RequestType.LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertEquals("here", req.getFacility().getFacilityName());
        assertNull(login.getLocalHostId());
    }
    
    @Test
    public void testReadLoginHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("1:steve|secret|:ID|"));
        assertEquals(RequestType.LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertEquals("there", login.getFacility().getFacilityName());
        assertEquals("ID", login.getLocalHostId());
    }
    
    @Test
    public void testReadStaffLogin() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("21:steve|secret|"));
        assertEquals(RequestType.STAFF_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertEquals("here", req.getFacility().getFacilityName());
        assertNull(login.getLocalHostId());
    }
    
    @Test
    public void testReadStaffLoginHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("21:steve|secret|:ID|"));
        assertEquals(RequestType.STAFF_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertEquals("there", login.getFacility().getFacilityName());
        assertEquals("ID", login.getLocalHostId());
    }
    
    @Test
    public void testReadVirtualLogin() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("11:steve|secret|?here|"));
        assertEquals(RequestType.VIRTUAL_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret", login.getPassword());
        assertEquals("here", login.getFacility().getFacilityName());
        assertNull(login.getLocalHostId());
    }
    
    @Test
    public void testReadNewVirtualLogin() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("14:steve|secret?|?here|"));
        assertEquals(RequestType.NEW_VIRTUAL_LOGIN, req.getType());
        LoginRequest login = (LoginRequest) req;
        assertEquals("steve", login.getUserName());
        assertEquals("secret?", login.getPassword());
        assertEquals("here", login.getFacility().getFacilityName());
        assertNull(login.getLocalHostId());
    }

    @Test
    public void testReadLogout() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("2:steve|]acc1|"));
        assertEquals(RequestType.LOGOUT, req.getType());
        LogoutRequest logout = (LogoutRequest) req;
        assertEquals("steve", logout.getUserName());
        assertEquals("acc1", logout.getAccount());
        assertEquals("here", req.getFacility().getFacilityName());
    }

    @Test
    public void testReadLogoutHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("2:steve|]acc1|:ID|"));
        assertEquals(RequestType.LOGOUT, req.getType());
        LogoutRequest logout = (LogoutRequest) req;
        assertEquals("steve", logout.getUserName());
        assertEquals("acc1", logout.getAccount());
        assertEquals("there", logout.getFacility().getFacilityName());
        assertEquals("ID", logout.getLocalHostId());
    }

    @Test
    public void testReadVirtualLogout() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("12:steve|secret|]acc1|?here|"));
        assertEquals(RequestType.VIRTUAL_LOGOUT, req.getType());
        LogoutRequest logout = (LogoutRequest) req;
        assertEquals("steve", logout.getUserName());
        assertEquals("acc1", logout.getAccount());
        assertEquals("here", logout.getFacility().getFacilityName());
    }

    @Test
    public void testReadAccount() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("3:steve|]acc1|"));
        assertEquals(RequestType.ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertEquals("here", req.getFacility().getFacilityName());
    }

    @Test
    public void testReadAccountHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("3:steve|]acc1|:ID|"));
        assertEquals(RequestType.ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertEquals("there", req.getFacility().getFacilityName());
        assertEquals("ID", req.getLocalHostId());
    }

    @Test
    public void testReadVirtualAccount() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("13:steve|]acc1|?here|"));
        assertEquals(RequestType.VIRTUAL_ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertEquals("here", acc.getFacility().getFacilityName());
        assertNull(req.getLocalHostId());
    }

    @Test
    public void testReadNewVirtualAccount() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("15:steve|]acc1|?here|"));
        assertEquals(RequestType.NEW_VIRTUAL_ACCOUNT, req.getType());
        AccountRequest acc = (AccountRequest) req;
        assertEquals("steve", acc.getUserName());
        assertEquals("acc1", acc.getAccount());
        assertEquals("here", acc.getFacility().getFacilityName());
        assertNull(req.getLocalHostId());
    }

    @Test
    public void testReadNotes() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source(
                "4:charlie|]unicorn|~Put a banana in your ear|"));
        assertEquals(RequestType.NOTES, req.getType());
        NoteRequest acc = (NoteRequest) req;
        assertEquals("charlie", acc.getUserName());
        assertEquals("unicorn", acc.getAccount());
        assertEquals("Put a banana in your ear", acc.getNotes());
        assertEquals("here", req.getFacility().getFacilityName());
        assertNull(req.getLocalHostId());
    }

    @Test
    public void testReadNotesHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source(
                "4:charlie|]unicorn|~Put a banana in your ear|:ID|"));
        assertEquals(RequestType.NOTES, req.getType());
        NoteRequest acc = (NoteRequest) req;
        assertEquals("charlie", acc.getUserName());
        assertEquals("unicorn", acc.getAccount());
        assertEquals("Put a banana in your ear", acc.getNotes());
        assertEquals("there", req.getFacility().getFacilityName());
        assertEquals("ID", req.getLocalHostId());
    }

    @Test
    public void testFacility() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("5:"));
        assertEquals(RequestType.FACILITY_NAME, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertEquals("here", req.getFacility().getFacilityName());
        assertNull(req.getLocalHostId());
    }
    
    @Test
    public void testFacilityHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("5:ID|"));
        assertEquals(RequestType.FACILITY_NAME, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertEquals("there", req.getFacility().getFacilityName());
        assertEquals("ID", req.getLocalHostId());
    }
    
    @Test
    public void testProject() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("6:"));
        assertEquals(RequestType.USE_PROJECT, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertEquals("here", req.getFacility().getFacilityName());
    }
    
    @Test
    public void testTimer() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("7:"));
        assertEquals(RequestType.USE_TIMER, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertEquals("here", req.getFacility().getFacilityName());
    }
    
    @Test
    public void testFullScreen() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("23:"));
        assertEquals(RequestType.USE_FULL_SCREEN, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testFullScreenHostId() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("23:"));
        assertEquals(RequestType.USE_FULL_SCREEN, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }

    @Test
    public void testFacilityType() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("8:"));
        assertEquals(RequestType.USE_VIRTUAL, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertNull(req.getLocalHostId());
    }
    
    @Test
    public void testFacilityCount() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("9:"));
        assertEquals(RequestType.FACILITY_COUNT, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertNull(req.getLocalHostId());
    }
    
    @Test
    public void testFacilityList() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("10:"));
        assertEquals(RequestType.FACILITY_LIST, req.getType());
        assertTrue(req instanceof SimpleRequest);
        assertNull(req.getLocalHostId());
    }
    
    @Test
    public void testSystemPassword() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("20:"));
        assertEquals(RequestType.SYSTEM_PASSWORD, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    @Test
    public void testNetDrive() throws AclsException {
        RequestReader r = reader();
        Request req = r.read(source("22:"));
        assertEquals(RequestType.NET_DRIVE, req.getType());
        assertTrue(req instanceof SimpleRequest);
    }
    
    private RequestReader reader() {
        return new RequestReaderImpl(mapper(), localHost());
    }
    
    private InetAddress localHost() {
        try {
            return InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            throw new AssertionError(ex);
        }
    }

    private FacilityMapper mapper() {
        StaticFacilityConfig here = new StaticFacilityConfig();
        here.setFacilityName("here");
        here.setAddress(localHost().getHostAddress());
        StaticFacilityConfig there = new StaticFacilityConfig();
        there.setFacilityName("there");
        there.setLocalHostId("ID");
        there.setAddress("nowhere.example.com");
        return new MockFacilityMapper(Arrays.asList(here, there));
    }

    private InputStream source(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError("UTF-8??");
        }
    }
}
