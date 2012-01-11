package org.bitrepository.webservice;

public class ServiceUrlFactory {
	private static ServiceUrl serviceUrl;

    /**
     *	Factory method to get a singleton instance of BasicClient
     *	@return The BasicClient instance or a null in case of trouble.  
     */
    public synchronized static ServiceUrl getInstance() {
        if(serviceUrl == null) {
        	serviceUrl = new ServiceUrl();
        }
        return serviceUrl;
         
    } 
}
