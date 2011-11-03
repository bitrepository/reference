package org.bitrepository.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.bitrepository.BasicClient;
import org.bitrepository.BasicClientFactory;
import org.bitrepository.GetFileIDsResults;

@Path("/reposervice")
public class Reposervice {
    
    private BasicClient client;
    
    
    public Reposervice() {
        client = BasicClientFactory.getInstance();
    }
    
    @GET
    @Path("/putfile/")
    @Produces("text/plain")
    public String putFile(
            @QueryParam("fileID") String fileID,
            @QueryParam("fileSize") long fileSize,
            @QueryParam("url") String URL) {
        return client.putFile(fileID, fileSize, URL);       
    }
    
    @GET
    @Path("/getfile/")
    @Produces("text/plain")
    public String getFile(
            @QueryParam("fileID") String fileID,
            @QueryParam("url") String URL) {
        return client.getFile(fileID, URL);       
    }
    
    @GET
    @Path("/getLog")
    @Produces("text/plain")
    public String getLog() {
        return client.getLog();
    }
    
    @GET
    @Path("/getHtmlLog")
    @Produces("text/html")
    public String getHtmlLog() {
        return client.getHtmlLog();
    }
    
    @GET
    @Path("/getShortHtmlLog")
    @Produces("text/html")
    public String getShortHtmlLog() {
    	return client.getShortHtmlLog();
    }
    
    @GET
    @Path("/getSettingsSummary")
    @Produces("text/plain")
    public String getSummarySettings() {
        return client.getSettingsSummary();
    }
    
    @GET
    @Path("getChecksumsHtml")
    @Produces("text/html")
    public String getChecksumsHtml(
    		@QueryParam("fileIDs") String fileIDs,
    		@QueryParam("checksumType") String checksumType,
    		@QueryParam("salt") String salt) {
    	
    	Map<String, Map<String, String>> result = client.getChecksums(fileIDs, checksumType, salt);
    	if(result == null) {
    		return "<html><body><b>Failed!</b></body></html>";
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html><body><table>");
    	Set<String> returnedFileIDs = result.keySet();
    	ArrayList <String> pillarIDList = new ArrayList<String>();
    	
    	sb.append("<tr> <td><b>File Id:</b></td><td>&nbsp;</td>");
    	for(String fileID : returnedFileIDs) {
    		Set<String> pillarIDs = result.get(fileID).keySet();
    		for(String pillarID : pillarIDs) {
    			pillarIDList.add(pillarID);
    			sb.append("<td><b>Checksums from " + pillarID + ":</b></td>");
    		}
    		break;
    	}
    	sb.append("</tr>");
    	for(String fileID : returnedFileIDs) {
    		sb.append("<tr> <td> " + fileID + "</td><td>&nbsp;</td>"); 
    		for(String pillarID : pillarIDList) {
    			if(result.get(fileID).containsKey(pillarID)) {
    				sb.append("<td> " + result.get(fileID).get(pillarID) + " </td>");	
    			} else {
    				sb.append("<td> unknown </td>");
    			}
    		}
    		sb.append("</tr>");
    	}
    	sb.append("</table></body></html>");
    	
    	return sb.toString();
    }
    
    @GET
    @Path("getChecksums")
    @Produces("text/plain")
    public String getChecksums(
    		@QueryParam("fileIDs") String fileIDs,
    		@QueryParam("checksumType") String checksumType,
    		@QueryParam("salt") String salt) {
    	
    	Map<String, Map<String, String>> result = client.getChecksums(fileIDs, checksumType, salt);
    	if(result == null) {
    		return "Failed!";
    	}
    	StringBuilder sb = new StringBuilder();
    	Set<String> returnedFileIDs = result.keySet();
    	ArrayList <String> pillarIDList = new ArrayList<String>();
    	
    	sb.append("FileID \t");
    	for(String fileID : returnedFileIDs) {
    		Set<String> pillarIDs = result.get(fileID).keySet();
    		for(String pillarID : pillarIDs) {
    			pillarIDList.add(pillarID);
    			sb.append(pillarID + "\t");
    		}
    		break;
    	}
    	sb.append("\n");
    	for(String fileID : returnedFileIDs) {
    		sb.append(fileID + "\t"); 
    		for(String pillarID : pillarIDList) {
    			if(result.get(fileID).containsKey(pillarID)) {
    				sb.append(result.get(fileID).get(pillarID) + "\t");	
    			} else {
    				sb.append("unknown \t");
    			}
    		}
    		sb.append("\n");
    	}
    	return sb.toString();
    }

    
    @GET
    @Path("getFileIDsHtml")
    @Produces("text/html")
    public String getFileIDsHtml(
    		@QueryParam("fileIDs") String fileIDs,
    		@QueryParam("allFileIDs") boolean allFileIDs) {
    	GetFileIDsResults results = client.getFileIDs(fileIDs, allFileIDs);
    	StringBuilder sb = new StringBuilder();
    	sb.append("<html><head><style> #good{background-color:#31B404;} #bad{background-color:#B40404;} " +
    			"td{padding: 5px;}</style></head><body>"); 
    	
    	sb.append("<table> <tr valign=\"top\"> \n <td>");
    	sb.append("<table> <tr> <th> <b>File Id:</b> </th> <th>&nbsp;&nbsp;" +
    			"</th><th><b>Number of answers </b></th></tr>");
    	Set<String> IDs = results.getResults().keySet();
    	String status;
    	for(String ID : IDs) {
    		if(results.getResults().get(ID).size() == results.getPillarList().size()) {
    			status = "good";
    		} else {
    			status = "bad";
    		}
    		sb.append("<tr><td id=" + status + ">" + ID + "</td><td></td><td>" + 
    				results.getResults().get(ID).size() +"</td></tr>");
    	}
    	sb.append("</table> </td> <td>&nbsp;&nbsp;</td><td><table><tr> <th> <b> Pillar list</b> </th> </tr>");
    	for(String pillar : results.getPillarList()) {
    		sb.append("<tr><td>" + pillar + "</td></tr>");
    	}
    	sb.append("</table></td> </tr> </table></body></html>");
    	
    	return sb.toString();
    }
    
}

//http://localhost:8080/webservice-0.0.1-SNAPSHOT/reposervice/getfile/?fileID=test (tomcat 7)
//http://localhost:8080/webservice-0.0.1-SNAPSHOT/reposervice/getLog/
//http://localhost:8080/webservice-0.0.1-SNAPSHOT/reposervice/getfile/?fileID=bah&url=http://sandkasse-01.kb.dk/dav/foobarbaz





