package org.bitrepository.audittrails.webservice;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bitrepository.audittrails.service.AuditTrailService;
import org.bitrepository.audittrails.service.AuditTrailServiceFactory;
import org.bitrepository.common.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Listener has two intentions
 * 1) Acquire necessary information at startup to locate configuration files and create the first instance 
 * 		of the basic client, so everything is setup before the first users start using the webservice. 
 * 2) In time shut the service down in a proper manner, so no threads will be orphaned.   
 */
public class ShutdownListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Do initialization work  
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	String confDir = sce.getServletContext().getInitParameter("integrityServiceConfDir");
	    if(confDir == null) {
	    	throw new RuntimeException("No configuration directory specified!");
	    }
	    log.debug("Configuration dir = " + confDir);
	    System.setProperty(ConfigurationFactory.CONFIGURATION_DIR_SYSTEM_PROPERTY, confDir);
	    //try {
			//new LogbackConfigLoader(confDir + "/logback.xml");
	//	} catch (Exception e) {
		//	log.info("Failed to read log configuration file. Falling back to default.");
	//	} 
	    AuditTrailServiceFactory.init(confDir);
	    AuditTrailService service = AuditTrailServiceFactory.getAuditTrailService();
        log.debug("Servlet context initialized");
    }

    /**
     * Do teardown work. 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Method that's run when the war file is undeployed. 
    	// Can be used to shut everything down nicely..
    	log.debug("Servlet context destroyed");
    }

}
