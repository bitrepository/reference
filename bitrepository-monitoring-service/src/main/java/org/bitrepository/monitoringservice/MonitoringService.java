package org.bitrepository.monitoringservice;

import java.util.Map;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.SecurityManager;

public class MonitoringService {

	/** The settings. */
    private final Settings settings;
    /** The security manager */
	private final SecurityManager securityManager;
	/** The store of collected statuses */
	private final ComponentStatusStore statusStore;
	/** The client for getting statuses. */
	private final GetStatusClient getStatusClient;
	/** The status collector */
	private final StatusCollector collector;
    
	public MonitoringService(Settings settings, SecurityManager securityManager) {
		this.settings = settings;
		this.securityManager = securityManager;
		statusStore = new ComponentStatusStore();
		getStatusClient = AccessComponentFactory.getInstance().createGetStatusClient(settings, securityManager);
		collector = new StatusCollector(getStatusClient, settings, statusStore);
	}
	
	public Map<String, ResultingStatus> getStatus() {
	    return statusStore.getStatusMap();
	}
	
	public void shutdown() {
	    collector.stop();
	    getStatusClient.shutdown();
	}
}
