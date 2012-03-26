package org.bitrepository.monitoringservice.webservice;

import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.monitoringservice.MonitoringService;
import org.bitrepository.monitoringservice.MonitoringServiceFactory;

@Path("/MonitoringService")
public class RestMonitoringService {

	private MonitoringService service;
	
	public RestMonitoringService() {
		service = MonitoringServiceFactory.getMonitoringService();
	}

	@GET
	@Path("/getMonitoringConfiguration/")
	@Produces("text/json")
	public String getMonitoringServiceConfiguration() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[");
        sb.append("{\"confOption\":\" Check interval \", \"confValue\": \" 60000 \"},");
        sb.append("{\"confOption\":\" Max retries \", \"confValue\": \" 3 \"},");
        sb.append("{\"confOption\":\" Timeout \", \"confValue\": \" 5000 \"}");
	    sb.append("]");
		return sb.toString();
	}
	
	public String getComponentStatuses() {
		return "";
	}
}
