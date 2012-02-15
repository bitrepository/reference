package org.bitrepository.integrityclient;

import java.util.Date;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrityService {
    private Logger log = LoggerFactory.getLogger(IntegrityService.class);
    private final SimpleIntegrityService service;
    private final Settings settings;
    
    public IntegrityService(SimpleIntegrityService simpleIntegrityService, Settings settings) {
    	this.service = simpleIntegrityService;
    	this.settings = settings;
    }
    
    public List<String> getPillarList() {
    	return settings.getCollectionSettings().getClientSettings().getPillarIDs();    	
    }
    
    public long getNumberOfFiles(String pillarID) {
    	return service.getNumberOfFiles(pillarID);
    }
    
    public long getNumberOfMissingFiles(String pillarID) {
    	return service.getNumberOfMissingFiles(pillarID);
    }
    
    public Date getDateForLastFileIDUpdate(String pillarID) {
    	return service.getDateForLastFileUpdate(pillarID);
    }
    
    public long getNumberOfChecksumErrors(String pillarID) {
    	return service.getNumberOfChecksumErrors(pillarID);
    }
    
    public Date getDateForLastChecksumUpdate(String pillarID) {
    	return service.getDateForLastChecksumUpdate(pillarID);
    }
    
    public long getSchedulingInterval() {
    	return settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval();
    }

}
