/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.integrityservice.web;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.IntegrityServiceFactory;
import org.bitrepository.service.scheduler.WorkflowTask;

@Path("/IntegrityService")
public class RestIntegrityService {
    private IntegrityServiceWebInterface service;
    
    public RestIntegrityService() {
        service = IntegrityServiceFactory.getIntegrityServiceWebInterface();
    }
    
    /**
     * Get the listing of integrity status as a JSON array
     */
    @GET
    @Path("/getIntegrityStatus/")
    @Produces("application/json")
    public String getIntegrityStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        List<String> pillars = service.getPillarList();
        Iterator<String> it = pillars.iterator();
        while(it.hasNext()) {
            String pillar = it.next();
            sb.append("{\"pillarID\": \"" + pillar + "\"," +
                    "\"totalFileCount\": \"" + service.getNumberOfFiles(pillar) + "\"," +
                    "\"missingFilesCount\": \"" + service.getNumberOfMissingFiles(pillar) + "\"," +
                    "\"checksumErrorCount\": \"" + service.getNumberOfChecksumErrors(pillar) + "\"}");
            if(it.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");       
        return sb.toString();
    }
    
    /***
     * Get the current workflows setup as a JSON array 
     */
    @GET
    @Path("/getWorkflowSetup/")
    @Produces("application/json")
    public String getWorkflowSetup() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Collection<WorkflowTask> workflows = service.getScheduledWorkflows();
        Iterator<WorkflowTask> it = workflows.iterator();
        while(it.hasNext()) {
            WorkflowTask workflowTasl = it.next();
            sb.append("{\"workflowID\": \"" + workflowTasl.getName() + "\"," +
                    "\"nextRun\": \"" + workflowTasl.getNextRun() + "\"," +
                    "\"executionInterval\": \"" + TimeUtils.millisecondsToHuman(workflowTasl.getTimeBetweenRuns()) + "\"}");
            if(it.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");       
        return sb.toString();
    }
    
    /**
     * Get the list of possible workflows as a JSON array 
     */
    @GET
    @Path("/getWorkflowList/")
    @Produces("application/json")
    public String getWorkflowList() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Collection<WorkflowTask> workflows = service.getScheduledWorkflows();
        Iterator<WorkflowTask> it = workflows.iterator();
        while(it.hasNext()) {
            String name = it.next().getName();
            sb.append("{\"workflowID\":\"" + name + "\"}");
            if(it.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Start a named workflow.  
     */
    @POST
    @Path("/startWorkflow/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public String startWorkflow(@FormParam ("workflowID") String workflowID) {
        Collection<WorkflowTask> workflows = service.getScheduledWorkflows();
        for(WorkflowTask workflowTask : workflows) {
            if(workflowTask.getName().equals(workflowID)) {
                workflowTask.trigger();
                return "Workflow '" + workflowID + "' started";        
            }
        }
        return "No workflow named '" + workflowID + "' was found!";
    }
    
}
