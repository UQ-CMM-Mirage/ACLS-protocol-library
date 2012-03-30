package au.edu.uq.cmm.aclslib.proxy;

import java.util.List;

import org.slf4j.*;

import au.edu.uq.cmm.aclslib.config.Configuration;
import au.edu.uq.cmm.aclslib.config.FacilityConfig;
import au.edu.uq.cmm.aclslib.message.AclsClient;
import au.edu.uq.cmm.aclslib.message.AclsException;
import au.edu.uq.cmm.aclslib.message.FacilityCountResponse;
import au.edu.uq.cmm.aclslib.message.FacilityListResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.service.ServiceException;
import au.edu.uq.cmm.aclslib.service.ThreadServiceBase;

public class FacilityChecker extends ThreadServiceBase {
    private static final Logger LOG = LoggerFactory.getLogger(FacilityChecker.class);

    private Configuration config;
    private AclsClient client;
    private int facilityCount = 0;

    public FacilityChecker(Configuration config) {
        this.config = config;
        this.client = new AclsClient(config.getServerHost(), config.getServerPort());
    }

    public void run() {
        long sleepMinutes = config.getFacilityRecheckInterval();
        try {
            while (true) {
                try {
                    checkFacilities();
                } catch (AclsException ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Facility check failed", ex);
                    } else {
                        LOG.info("Facility check failed: " + ex.getMessage());
                    }
                }
                if (sleepMinutes > 0) {
                    Thread.sleep(sleepMinutes * 60 * 1000);
                } else {
                    // Wait until interrupted.
                    Object lock = new Object();
                    synchronized (lock) {
                        lock.wait();
                    }
                }
            }
        } catch (InterruptedException ex) {
            LOG.info("Interrupted - we're done");
        }
    }

    private void checkFacilities() throws AclsException {
        int newCount = queryFacilityCount();
        if (facilityCount == newCount) {
            LOG.debug("Facility count hasn't changed");
            return;
        }
        LOG.debug("New count is " + newCount);
        facilityCount = newCount;
        List<String> facilityIds = queryFacilityList();
        LOG.debug("Facility list - " + facilityIds);
        for (String id : facilityIds) {
            FacilityConfig facility = config.lookupFacilityByName(id);
            if (facility == null) {
                LOG.error("The server has a facility that we don't recognize: " + id);
            }
        }
    }

    private List<String> queryFacilityList() throws AclsException {
        Request request = new SimpleRequest(
                RequestType.FACILITY_LIST, null, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case FACILITY_LIST:
            return ((FacilityListResponse) response).getList();
        default:
            throw new ServiceException("Unexpected response: " + response.getType());
        }
    }

    private int queryFacilityCount() throws ServiceException, AclsException {
        Request request = new SimpleRequest(
                RequestType.FACILITY_COUNT, null, null, null);
        Response response = client.serverSendReceive(request);
        switch (response.getType()) {
        case FACILITY_COUNT:
            return ((FacilityCountResponse) response).getCount();
        default:
            throw new ServiceException("Unexpected response: " + response.getType());
        }
    }
}
