package org.bitrepository.monitoringservice;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.SecurityManager;

public class MonitoringService {

	/** The settings. */
    private final Settings settings;
    /** The security manager */
	private final SecurityManager securityManager;
    
	public MonitoringService(Settings settings, SecurityManager securityManager) {
		this.settings = settings;
		this.securityManager = securityManager;
	}
}
