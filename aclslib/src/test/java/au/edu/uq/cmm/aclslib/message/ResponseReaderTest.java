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
    public void testBadStatusLine() {
        reader().readWithStatusLine(source("Bad wolf\n0:\n"));
    }
    
    @Test
    public void testGoodStatusLine() {
        Message m = reader().readWithStatusLine(source("IP Accepted\n0:\n"));
        assertTrue(m instanceof CommandErrorResponse);
    }
    
    @Test
    public void testCommandError() {
        Message m = reader().read(source("0:\n"));
        assertTrue(m instanceof CommandErrorResponse);
    }
    
    @Test
    public void testCommandError2() {
        Message m = reader().read(source("0:"));
        assertTrue(m instanceof CommandErrorResponse);
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
    
    @Test(expected=MessageSyntaxException.class)
    public void testBadCommand5() {
        reader().read(source("0:whatever\n"));
    }
    
    @Test
    public void testLogin() {
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
    public void testVirtualLogin() {
        Response r = reader().read(source("111:steve|cmm|]acc1;|&Valid Certificate~No|\n"));
        assertEquals(ResponseType.VIRTUAL_LOGIN_ALLOWED, r.getType());
        LoginResponse login = (LoginResponse) r;
        assertEquals("steve", login.getUserName());
        assertEquals("cmm", login.getOrgName());
        assertEquals(1, login.getAccounts().size());
        assertEquals("acc1", login.getAccounts().get(0));
        assertEquals(Certification.VALID, login.getCertification());
        assertEquals(false, login.isOnsiteAssist());
    }
    
    @Test
    public void testNewVirtualLogin() {
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
    public void testLoginRefused() {
        Response r = reader().read(source("12:\n"));
        assertEquals(ResponseType.LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testVirtualLoginRefused() {
        Response r = reader().read(source("112:\n"));
        assertEquals(ResponseType.VIRTUAL_LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testNewVirtualLoginRefused() {
        Response r = reader().read(source("142:\n"));
        assertEquals(ResponseType.NEW_VIRTUAL_LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testStaffLoginRefused() {
        Response r = reader().read(source("212:\n"));
        assertEquals(ResponseType.STAFF_LOGIN_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testStaffLoginAllowed() {
        Response r = reader().read(source("211:\n"));
        assertEquals(ResponseType.STAFF_LOGIN_ALLOWED, r.getType());
        assertTrue(r instanceof AllowedResponse);
    }
    
    @Test
    public void testLogoutRefused() {
        Response r = reader().read(source("22:\n"));
        assertEquals(ResponseType.LOGOUT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testLogoutAlloweded() {
        Response r = reader().read(source("21:\n"));
        assertEquals(ResponseType.LOGOUT_ALLOWED, r.getType());
    }
    
    @Test
    public void testAccountRefused() {
        Response r = reader().read(source("32:\n"));
        assertEquals(ResponseType.ACCOUNT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testVirtualAccountRefused() {
        Response r = reader().read(source("132:\n"));
        assertEquals(ResponseType.VIRTUAL_ACCOUNT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testNewVirtualAccountRefused() {
        Response r = reader().read(source("152:\n"));
        assertEquals(ResponseType.NEW_VIRTUAL_ACCOUNT_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testAccountAllowed() {
        Response r = reader().read(source("31:[2012-01-01T00:00:00Z|\n"));
        assertEquals(ResponseType.ACCOUNT_ALLOWED, r.getType());
        assertTrue(r instanceof AccountResponse);
        assertEquals("2012-01-01T00:00:00Z", ((AccountResponse) r).getLoginTimestamp());
    }
    
    @Test
    public void testVirtualAccountAllowed() {
        Response r = reader().read(source("31:[2012-01-01T00:00:00Z|\n"));
        assertEquals(ResponseType.ACCOUNT_ALLOWED, r.getType());
        assertTrue(r instanceof AccountResponse);
        assertEquals("2012-01-01T00:00:00Z", ((AccountResponse) r).getLoginTimestamp());
    }
    
    @Test
    public void testNewVirtualAccountAllowed() {
        Response r = reader().read(source("31:[2012-01-01T00:00:00Z|\n"));
        assertEquals(ResponseType.ACCOUNT_ALLOWED, r.getType());
        assertTrue(r instanceof AccountResponse);
        assertEquals("2012-01-01T00:00:00Z", ((AccountResponse) r).getLoginTimestamp());
    }
    
    @Test
    public void testNoteAllowed() {
        Response r = reader().read(source("41:\n"));
        assertEquals(ResponseType.NOTES_ALLOWED, r.getType());
        assertTrue(r instanceof AllowedResponse);
    }
    
    @Test
    public void testNoteRefused() {
        Response r = reader().read(source("42:\n"));
        assertEquals(ResponseType.NOTES_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testFacilityAllowed() {
        Response r = reader().read(source("51:?erehwon|\n"));
        assertEquals(ResponseType.FACILITY_ALLOWED, r.getType());
        assertTrue(r instanceof FacilityNameResponse);
        FacilityNameResponse f = (FacilityNameResponse) r;
        assertEquals("erehwon", f.getFacility());
    }
    
    @Test
    public void testFacilityRefused() {
        Response r = reader().read(source("52:\n"));
        assertEquals(ResponseType.FACILITY_REFUSED, r.getType());
        assertTrue(r instanceof RefusedResponse);
    }
    
    @Test
    public void testProjectYes() {
        Response r = reader().read(source("61:\n"));
        assertEquals(ResponseType.PROJECT_YES, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testProjectNo() {
        Response r = reader().read(source("62:\n"));
        assertEquals(ResponseType.PROJECT_NO, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testTimerYes() {
        Response r = reader().read(source("71:\n"));
        assertEquals(ResponseType.TIMER_YES, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testTimerNo() {
        Response r = reader().read(source("72:\n"));
        assertEquals(ResponseType.TIMER_NO, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFullScreenYes() {
        Response r = reader().read(source("231:\n"));
        assertEquals(ResponseType.FULL_SCREEN_YES, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFullScreenNo() {
        Response r = reader().read(source("232:\n"));
        assertEquals(ResponseType.FULL_SCREEN_NO, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFacilityYes() {
        Response r = reader().read(source("81:?Yes|\n"));
        assertEquals(ResponseType.USE_VIRTUAL, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertTrue(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFacilityNo() {
        Response r = reader().read(source("81:?No|\n"));
        assertEquals(ResponseType.USE_VIRTUAL, r.getType());
        assertTrue(r instanceof YesNoResponse);
        assertFalse(((YesNoResponse) r).isYes());
    }
    
    @Test
    public void testFacilityCount() {
        Response r = reader().read(source("91:?42|\n"));
        assertEquals(ResponseType.FACILITY_COUNT, r.getType());
        assertTrue(r instanceof FacilityCountResponse);
        assertEquals(42, ((FacilityCountResponse) r).getCount());
    }
    
    @Test
    public void testFacilityList() {
        Response r = reader().read(source("101:?f1;f2;f3;|\n"));
        assertEquals(ResponseType.FACILITY_LIST, r.getType());
        assertTrue(r instanceof FacilityListResponse);
        assertEquals(3, ((FacilityListResponse) r).getList().size());
        assertEquals("f1", ((FacilityListResponse) r).getList().get(0));
        assertEquals("f2", ((FacilityListResponse) r).getList().get(1));
        assertEquals("f3", ((FacilityListResponse) r).getList().get(2));
    }
    
    @Test
    public void testSystemPasswordNo() {
        Response r = reader().read(source("202:\n"));
        assertEquals(ResponseType.SYSTEM_PASSWORD_NO, r.getType());
        assertTrue(r instanceof SystemPasswordResponse);
        assertNull(((SystemPasswordResponse) r).getPassword());
    }
    
    @Test
    public void testSystemPasswordYes() {
        Response r = reader().read(source("201:/hi mum!|\n"));
        assertEquals(ResponseType.SYSTEM_PASSWORD_YES, r.getType());
        assertTrue(r instanceof SystemPasswordResponse);
        assertEquals("hi mum!", ((SystemPasswordResponse) r).getPassword());
    }
    
    @Test
    public void testNetDriveNo() {
        Response r = reader().read(source("222:\n"));
        assertEquals(ResponseType.NET_DRIVE_NO, r.getType());
        assertTrue(r instanceof NetDriveResponse);
        assertNull(((NetDriveResponse) r).getDriveName());
    }
    
    @Test
    public void testNetDriveYes() {
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
    public void testNetDriveYesEmpty() {
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
