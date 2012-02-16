package org.bitrepository.audittrails.service;

public class AuditTrailServiceFactory {
 
	private static AuditTrailService auditTrailService;
	private static String configurationDir;

	/**
	 * Private constructor as the class is meant to be used in a static way.
	 */
	private AuditTrailServiceFactory() {
		
	}
	
	
	public static void init(String confDir) {
		configurationDir = confDir;
	}
	
	/**
	 * Factory method to retrieve AuditTrailService  
	 */
	public synchronized static AuditTrailService getAuditTrailService() {
		if(auditTrailService == null) {
			auditTrailService = new AuditTrailService();
		}
		
		return auditTrailService;
	}
}
