package org.bitrepository.monitoringservice.webservice;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
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
	@Produces("application/json")
	public String getMonitoringServiceConfiguration() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[");
        sb.append("{\"confOption\":\" Check interval \", \"confValue\": \" 60000 \"},");
        sb.append("{\"confOption\":\" Max retries \", \"confValue\": \" 3 \"},");
        sb.append("{\"confOption\":\" Timeout \", \"confValue\": \" 5000 \"}");
	    sb.append("]");
		return sb.toString();
	}
	
	@GET
    @Path("/getComponentStatus/")
    @Produces("application/json")
	public String getComponentStatus() {
	    Map<String, ResultingStatus> statusMap = service.getStatus();
	    Set<String> components = statusMap.keySet();
	    StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator<String> it = components.iterator();
        while(it.hasNext()) {
            String component = it.next();
            sb.append("{\"componentID\": \"" + component + "\"," +
                    "\"status\":\" " + statusMap.get(component).getStatusInfo().getStatusCode() + "\"," + 
                    "\"info\":\" " + statusMap.get(component).getStatusInfo().getStatusText() + "\"," + 
                    "\"timeStamp\":\" " + statusMap.get(component).getStatusTimestamp() + "\"" + 
                "}");
            if(it.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
	}
}
