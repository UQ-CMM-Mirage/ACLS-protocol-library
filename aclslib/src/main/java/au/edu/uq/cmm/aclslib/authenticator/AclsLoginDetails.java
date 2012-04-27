package au.edu.uq.cmm.aclslib.authenticator;

import java.util.List;

import au.edu.uq.cmm.aclslib.message.Certification;

/**
 * This represents the information returned by ACLS when a user logs in (successfully).
 * 
 * @author scrawley
 *
 */
public class AclsLoginDetails {
    private String userName;
    private String orgName;
    private String password;
    private String facilityName;
    private List<String> accounts;
    private Certification certification;
    private boolean onsiteAssist;
    private boolean cached;
    
    public AclsLoginDetails(String userName, String orgName, String password,
            String facilityName, List<String> accounts, Certification certification,
            boolean onsiteAssist, boolean cached) {
        super();
        this.userName = userName;
        this.password = password;
        this.accounts = accounts;
        this.certification = certification;
        this.onsiteAssist = onsiteAssist;
        this.facilityName = facilityName;
        this.cached = cached;
    }

    public String getUserName() {
        return userName;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public Certification getCertification() {
        return certification;
    }

    public boolean isOnsiteAssist() {
        return onsiteAssist;
    }

    public boolean isCached() {
        return cached;
    }
}