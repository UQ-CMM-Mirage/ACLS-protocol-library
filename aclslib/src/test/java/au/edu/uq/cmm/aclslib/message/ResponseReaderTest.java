package au.edu.uq.cmm.aclslib.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class ResponseReaderTest {

    @Test
    public void testConstructor() {
        new ResponseReaderImpl();
    }

    @Test(expected=ServerStatusException.class)
    public void testBadStatusLine() throws AclsException {
        reader().readWithStatusLine(source("Bad wolf\n0:\n"));
    }
    
    @Test
    public void testGoodStatusLine() throws AclsException {
        Message m = reader().readWithStatusLine(source("IP Accepted\n0:\n"));
        assertTrue(m instanceof CommandErrorResponse);
    }
    
    @Test
    public void testCommandError() throws AclsException {
        Message m = reader().read(source("0:\n"));
        assertTrue(m instanceof CommandErrorResponse);
    }
    
    @Test
    public void testCommandError2() throws AclsException {
        Message m = reader().read(source("0:"));
        assertTrue(m instanceof CommandErrorResponse);
    }
    
    @Test(expected=AclsMessageSyntaxException.class)
    public void testBadCommand() throws AclsException {
        reader().read(source("Z:\n"));
    }
    
    @Test(expected=AclsMessageSyntaxException.class)
    public void testBadCommand2() throws AclsException {
        reader().read(source("1\n"));
    }
    
    @Test(expected=AclsMessageSyntaxException.class)
    public void testBadCommand3() throws AclsException {
        reader().read(source("1?\n"));
    }
    
    @Test(expected=AclsMessageSyntaxException.class)
    public void testBadCommand4() throws AclsException {
        reader().read(source("9999:\n"));
    }
    
    @Test(expected=AclsMessageSyntaxException.class)
    public void testBadCommand5() throws AclsException {
        reader().read(source("0:whatever\n"));
    }
    
    @Test
    public void testLogin() throws AclsException {
        Response r = reader().read(source("11:steve|cmm|]acc1;|&Valid Certificate~No|\n"));
        assertEquals(ResponseType.LOGIN_ALLOWED, r.getType());
        LoginResponse login = (LoginResponse) r;
        assertEquals("steve", login.getUserName());
        assertEquals("cmm", login.getOrgName());
        assertEquals(1, login.getAccounts().size());
        assertEquals("acc1", login.getAccounts().get(0));
        assertEquals(Certification.VALID, login.getCertification());
        assertEquals(false, login.isOnsiteAssist());
    }
    
    @Test
    public void testLogin2() throws AclsException {
        Response r = reader().read(source("11:test|cmm|]general;special;|&No Certificate~No|\n"));
        assertEquals(ResponseType.LOGIN_ALLOWED, r.getType());
        LoginResponse login = (LoginResponse) r;
        assertEquals("test", login.getUserName());
        assertEquals("cmm", login.getOrgName());
        assertEquals(2, login.getAccounts().size());
        assertEquals("general", login.getAccounts().get(0));
        assertEquals("special", login.getAccounts().get(1));
        assertEquals(Certification.NONE, login.getCertification());
        assertEquals(false, login.isOnsiteAssist());
    }
    
    @Test
    public void testVirtualLogin() throws AclsException {
        Response r = reader().read(source("111:steve|cmm|]acc1;|&Valid Certificate~Yes|\n"));
        assertEquals(ResponseType.VIRTUAL_LOGIN_ALLOWED, r.getType());
        LoginResponse login = (LoginResponse) r;
        assertEquals("steve", login.getUserName());
        assertEquals("cmm", login.getOrgName());
        assertEquals(1, login.getAccounts().size());
        assertEquals("acc1", login.getAccounts().get(0));
        assertEquals(Certification.VALID, login.getCertification());
        assertEquals(true, login.isOnsiteAssist());
    }
    
    @Test
    public void testVirtualLogin2() throws AclsException {
        Response r = reader().read(
                source("111:steve|cmm|[2012-01-01T00:00:00Z|]acc1;|&Valid Certificate~Yes|\n"));
        assertEquals(ResponseType.VIRTUAL_LOGIN_ALLOWED, r.getType());
        LoginResponse login = (LoginResponse) r;
        assertEquals("steve", login.getUserName());
        assertEquals("cmm", login.getOrgName());
        assertEquals("2012-01-01T00:00:00Z", login.getLoginTimestamp());
        assertEquals(1, login.getAccounts().size());
        assertEquals("acc1", login.getAccounts().get(0));
        assertEquals(Certification.VALID, login.getCertification());
        assertEquals(true, login.isOnsiteAssist());
    }
    
    @Test
    public void testNewVirtualLogin() throws AclsException {
        Response r = reader().read(source("141:steve|cmm|]acc1;|&Valid Certificate~No|\n"));
        assertEquals(ResponseType.NEW_VIRTUAL_LOGIN_ALLOWED, r.getType());
        LoginResponse login = (LoginResponse) r;
        assertEquals("steve", login.getUserName());
        assertEquals("cmm", login.getOrgName());
        assertEquals(1, login.getAccounts().size());
        assertEquals("acc1", login.getAccounts().get(0));
        assertEquals(Certification.VALID, login.getCertification());
        assertEquals(false, login.isOnsiteAssist());
    }
    
    @Test
    public void testLoginRefused() throws AclsException {
        Response r = reader().read(source("12:\n"));
        assertEquals(ResponseType.LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testVirtualLoginRefused() throws AclsException {
        Response r = reader().read(source("112:\n"));
        assertEquals(ResponseType.VIRTUAL_LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testNewVirtualLoginRefused() throws AclsException {
        Response r = reader().read(source("142:\n"));
        assertEquals(ResponseType.NEW_VIRTUAL_LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testStaffLoginRefused() throws AclsException {
        Response r = reader().read(source("212:\n"));
        assertEquals(ResponseType.STAFF_LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testStaffLoginAllowed() throws AclsException {
        Response r = reader().read(source("211:\n"));
        assertEquals(ResponseType.STAFF_LOGIN_ALLOWED, r.getType());
        assertTrue(r instanceof AllowedResponse);
    }
    
    @Test
    public void testLogoutRefused() throws AclsException {
        Response r = reader().read(source("22:\n"));
        assertEquals(ResponseType.LOGOUT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testLogoutAllowed() throws AclsException {
        Response r = reader().read(source("21:\n"));
        assertEquals(ResponseType.LOGOUT_ALLOWED, r.getType());
    }
    
    @Test
    public void testAccountRefused() throws AclsException {
        Response r = reader().read(source("32:\n"));
        assertEquals(ResponseType.ACCOUNT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testVirtualAccountRefused() throws AclsException {
        Response r = reader().read(source("132:\n"));
        assertEquals(ResponseType.VIRTUAL_ACCOUNT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testNewVirtualAccountRefused() throws AclsException {
        Response r = reader().read(source("152:\n"));
        assertEquals(ResponseType.NEW_VIRTUAL_ACCOUNT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testAccountAllowed() throws AclsException {
        Response r = reader().read(source("31:[2012-01-01T00:00:00Z|\n"));
        assertEquals(ResponseType.ACCOUNT_ALLOWED, r.getType());
        assertTrue(r instanceof AccountResponse);
        assertEquals("2012-01-01T00:00:00Z", ((AccountResponse) r).getLoginTimestamp());
    }
    
    @Test
    public void testVirtualAccountAllowed() throws AclsException {
        Response r = reader().read(source("31:[2012-01-01T00:00:00Z|\n"));
        assertEquals(ResponseType.ACCOUNT_ALLOWED, r.getType());
        assertTrue(r instanceof AccountResponse);
        assertEquals("2012-01-01T00:00:00Z", ((AccountResponse) r).getLoginTimestamp());
    }
    
    @Test
    public void testNewVirtualAccountAllowed() throws AclsException {
        Response r = reader().read(source("31:[2012-01-01T00:00:00Z|\n"));
        assertEquals(ResponseType.ACCOUNT_ALLOWED, r.getType());
        assertTrue(r instanceof AccountResponse);
        assertEquals("2012-01-01T00:00:00Z", ((AccountResponse) r).getLoginTimestamp());
    }
    
    @Test
    public void testNoteAllowed() throws AclsException {
        Response r = reader().read(source("41:\n"));
        assertEquals(ResponseType.NOTES_ALLOWED, r.getType());
        assertTrue(r instanceof AllowedResponse);
    }
    
    @Test
    public void testNoteRefused() throws AclsException {
        Response r = reader().read(source("42:\n"));
        assertEquals(ResponseType.NOTES_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testFacilityAllowed() throws AclsException {
        Response r = reader().read(source("51:?erehwon|\n"));
        assertEquals(ResponseType.FACILITY_ALLOWED, r.getType());
        assertTrue(r instanceof FacilityNameResponse);
        FacilityNameResponse f = (FacilityNameResponse) r;
        assertEquals("erehwon", f.getFacility());
    }
    
    @Test
    public void testFacilityRefused() throws AclsException {
        Response r = reader().read(source("52:\n"));
        assertEquals(ResponseType.FACILITY_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testProjectYes() throws AclsException {
        Response r = reader().read(source("61:\n"));
        assertEquals(ResponseType.PROJECT_YES, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testProjectNo() throws AclsException {
        Response r = reader().read(source("62:\n"));
        assertEquals(ResponseType.PROJECT_NO, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testTimerYes() throws AclsException {
        Response r = reader().read(source("71:\n"));
        assertEquals(ResponseType.TIMER_YES, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testTimerNo() throws AclsException {
        Response r = reader().read(source("72:\n"));
        assertEquals(ResponseType.TIMER_NO, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFullScreenYes() throws AclsException {
        Response r = reader().read(source("231:\n"));
        assertEquals(ResponseType.FULL_SCREEN_YES, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFullScreenNo() throws AclsException {
        Response r = reader().read(source("232:\n"));
        assertEquals(ResponseType.FULL_SCREEN_NO, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFacilityYes() throws AclsException {
        Response r = reader().read(source("81:?vMFL|\n"));
        assertEquals(ResponseType.USE_VIRTUAL, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFacilityNo() throws AclsException {
        Response r = reader().read(source("81:?No|\n"));
        assertEquals(ResponseType.USE_VIRTUAL, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFacilityCount() throws AclsException {
        Response r = reader().read(source("91:?42|\n"));
        assertEquals(ResponseType.FACILITY_COUNT, r.getType());
        assertTrue(r instanceof FacilityCountResponse);
        assertEquals(42, ((FacilityCountResponse) r).getCount());
    }
    
    @Test
    public void testFacilityList() throws AclsException {
        Response r = reader().read(source("101:;f1;f2;f3;|\n"));
        assertEquals(ResponseType.FACILITY_LIST, r.getType());
        assertTrue(r instanceof FacilityListResponse);
        assertEquals(3, ((FacilityListResponse) r).getList().size());
        assertEquals("f1", ((FacilityListResponse) r).getList().get(0));
        assertEquals("f2", ((FacilityListResponse) r).getList().get(1));
        assertEquals("f3", ((FacilityListResponse) r).getList().get(2));
    }
    
    @Test
    public void testSystemPasswordNo() throws AclsException {
        Response r = reader().read(source("202:\n"));
        assertEquals(ResponseType.SYSTEM_PASSWORD_NO, r.getType());
        assertTrue(r instanceof SystemPasswordResponse);
        assertNull(((SystemPasswordResponse) r).getPassword());
    }
    
    @Test
    public void testSystemPasswordYes() throws AclsException {
        Response r = reader().read(source("201:/hi mum!|\n"));
        assertEquals(ResponseType.SYSTEM_PASSWORD_YES, r.getType());
        assertTrue(r instanceof SystemPasswordResponse);
        assertEquals("hi mum!", ((SystemPasswordResponse) r).getPassword());
    }
    
    @Test
    public void testNetDriveNo() throws AclsException {
        Response r = reader().read(source("222:\n"));
        assertEquals(ResponseType.NET_DRIVE_NO, r.getType());
        assertTrue(r instanceof NetDriveResponse);
        assertNull(((NetDriveResponse) r).getDriveName());
    }
    
    @Test
    public void testNetDriveYes() throws AclsException {
        Response r = reader().read(source("221:Z]/foo/bar[joe~secret|\n"));
        assertEquals(ResponseType.NET_DRIVE_YES, r.getType());
        assertTrue(r instanceof NetDriveResponse);
        NetDriveResponse nd = (NetDriveResponse) r;
        assertEquals("Z", nd.getDriveName());
        assertEquals("/foo/bar", nd.getFolderName());
        assertEquals("joe", nd.getAccessName());
        assertEquals("secret", nd.getAccessPassword());
    }
    
    @Test
    public void testNetDriveYesEmpty() throws AclsException {
        Response r = reader().read(source("221:][~|\n"));
        assertEquals(ResponseType.NET_DRIVE_YES, r.getType());
        assertTrue(r instanceof NetDriveResponse);
        NetDriveResponse nd = (NetDriveResponse) r;
        assertEquals("", nd.getDriveName());
        assertEquals("", nd.getFolderName());
        assertEquals("", nd.getAccessName());
        assertEquals("", nd.getAccessPassword());
    }
    
    //
    // Test helper methods ...
    //
    
    private ResponseReader reader() {
        return new ResponseReaderImpl();
    }
    
    private InputStream source(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError("UTF-8??");
        }
    }
}
