package org.bitrepository.webservice;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bitrepository.BasicClient;
import org.bitrepository.BasicClientFactory;
import org.bitrepository.utils.LogbackConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Listener has two intentions
 * 1) Acquire necessary information at startup to locate configuration files and create the first instance 
 * 		of the basic client, so everything is setup before the first users start using the webservice. 
 * 2) In time shut the service down in a proper manner, so no threads will be orphaned.   
 */
public class WebclientContextListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Do initialization work  
     */
    @SuppressWarnings("unused")
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String confDir = sce.getServletContext().getInitParameter("configurationDir");
        if(confDir == null) {
        	throw new RuntimeException("No configuration directory specified!");
        }
        log.debug("Configuration dir = " + confDir);
        try {
			new LogbackConfigLoader(confDir + "/logback.xml");
		} catch (Exception e) {
			log.info("Failed to read log configuration file. Falling back to default.");
		} 
        BasicClientFactory.init(confDir);
        BasicClient client = BasicClientFactory.getInstance();
        log.debug("Servlet context initialized");
    }

    /**
     * Does work of shutting the webclient down in a graceful manner. 
     * This is done by calling BasicClient's shutdown method.  
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        BasicClient client = BasicClientFactory.getInstance();
        client.shutdown(); 
        log.debug("Servlet context destroyed");
    }

}
