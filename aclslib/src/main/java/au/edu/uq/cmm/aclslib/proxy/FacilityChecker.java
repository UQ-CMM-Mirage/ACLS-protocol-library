package au.edu.uq.cmm.aclslib.proxy;

import java.util.List;

import org.apache.log4j.Logger;

import au.edu.uq.cmm.aclslib.message.FacilityCountResponse;
import au.edu.uq.cmm.aclslib.message.FacilityListResponse;
import au.edu.uq.cmm.aclslib.message.Request;
import au.edu.uq.cmm.aclslib.message.RequestType;
import au.edu.uq.cmm.aclslib.message.Response;
import au.edu.uq.cmm.aclslib.message.SimpleRequest;
import au.edu.uq.cmm.aclslib.server.Configuration;
import au.edu.uq.cmm.aclslib.server.Facility;
import au.edu.uq.cmm.aclslib.service.ServiceException;
import au.edu.uq.cmm.aclslib.service.ThreadServiceBase;

public class FacilityChecker extends ThreadServiceBase {
    private static final Logger LOG = Logger.getLogger(FacilityChecker.class);

    private Configuration config;
    private int facilityCount = 0;

    public FacilityChecker(Configuration config) {
        this.config = config;
    }

    public void run() {
        while (true) {
            checkFacilities();
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException ex) {
                break;
            }
        } 
    }

    private void checkFacilities() {
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
            Facility facility = config.lookupFacilityById(id);
            if (facility == null) {
                LOG.error("The server has a facility that we don't recognize: " + id);
            }
        }
    }

    private List<String> queryFacilityList() {
        Request request = new SimpleRequest(RequestType.FACILITY_LIST);
        Response response = RequestProcessor.serverSendReceive(request, config);
        switch (response.getType()) {
        case FACILITY_LIST:
            return ((FacilityListResponse) response).getList();
        default:
            throw new ServiceException("Unexpected response: " + response.getType());
        }
    }

    private int queryFacilityCount() throws ServiceException {
        Request request = new SimpleRequest(RequestType.FACILITY_COUNT);
        Response response = RequestProcessor.serverSendReceive(request, config);
        switch (response.getType()) {
        case FACILITY_COUNT:
            return ((FacilityCountResponse) response).getCount();
        default:
            throw new ServiceException("Unexpected response: " + response.getType());
        }
    }
}
