package org.bitrepository.integrityclient.web;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.integrityclient.IntegrityService;
import org.bitrepository.integrityclient.IntegrityServiceFactory;

@Path("/IntegrityService")
public class RestIntegrityService {
	private IntegrityService service;
	
	public RestIntegrityService() {
		service = IntegrityServiceFactory.getIntegrityService();
	}
    
    @GET
    @Path("/getIntegrityStatus/")
    @Produces("text/html")
    public String getIntegrityStatus() {
    	StringBuilder sb = new StringBuilder();
		sb.append("<table id=\"users\" class=\"ui-widget ui-widget-content\">\n");
		sb.append("<thead>\n");
		sb.append("<tr class=\"ui-widget-header\">\n");
		sb.append("<th width=\"100\">PillarID</th>\n");
		sb.append("<th width=\"100\">Total number of files</th>\n");
		sb.append("<th width=\"100\">Number of missing files</th>\n");
		sb.append("<th width=\"100\">Last file list update</th>\n");
		sb.append("<th width=\"100\">Number of checksum errors</th>\n");
		sb.append("<th>Last checksum update</th>\n");
		sb.append("</tr>\n");
		sb.append("</thead>\n");
		sb.append("<tbody>\n");
		List<String> pillars = service.getPillarList();
		for(String pillar : pillars) {
			sb.append("<tr> \n");
			sb.append("<td>" + pillar + " </td>\n");
			sb.append("<td>" + service.getNumberOfFiles(pillar) + " </td>\n");
			sb.append("<td>" + service.getNumberOfMissingFiles(pillar) + " </td>\n");
			sb.append("<td>" + service.getDateForLastFileIDUpdate(pillar) + " </td>\n");
			sb.append("<td>" + service.getNumberOfChecksumErrors(pillar) + " </td>\n");
			sb.append("<td>" + service.getDateForLastChecksumUpdate(pillar) + " </td>\n");
			sb.append("</tr>\n");
		}
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		return sb.toString();
    }
    
    @GET
    @Path("/getSchedulerSetup/")
    @Produces("text/html")
    public String getSchedulerSetup() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<table class=\"ui-widget ui-widget-content\">\n");
		sb.append("<thead>\n");
		sb.append("<tr class=\"ui-widget-header\">\n");
		sb.append("<th width=\"200\">Configuration name</th>\n");
		sb.append("<th>Value</th>\n");
		sb.append("</tr>\n");
		sb.append("</thead>\n");
		sb.append("<tbody>\n");
    	sb.append("<tr><td>Scheduler interval</td><td>" + service.getSchedulingInterval() + "</td></tr>\n");
    	sb.append("</table>\n");
    	return sb.toString();
    }
    
    @POST
    @Path("/startFileIDCheckFromPillar/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public String startFileIDCheckFromPillar(@FormParam ("pillarID") String pillarID) {
    	return "Starting collection of fileID's from pillar: " + pillarID + "\n";
    }
    
    @POST
    @Path("/startChecksumCheckFromPillar/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public String startChecksumCheckFromPillar(@FormParam ("pillarID") String pillarID) {
    	return "Starting collection of checksums from pillar: " + pillarID + "\n";
    }
    
}
