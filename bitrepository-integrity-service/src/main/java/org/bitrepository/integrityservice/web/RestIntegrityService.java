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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.bitrepository.common.utils.FileSizeUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.IntegrityServiceManager;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.PillarCollectionStat;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReportConstants.ReportPart;
import org.bitrepository.integrityservice.reports.IntegrityReportProvider;
import org.bitrepository.integrityservice.reports.IntegrityReportReader;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowManager;
import org.bitrepository.service.workflow.WorkflowStatistic;
import org.bitrepository.settings.referencesettings.PillarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Path("/IntegrityService")
public class RestIntegrityService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final IntegrityModel model;
    private final WorkflowManager workflowManager;
    private final IntegrityReportProvider integrityReportProvider;

    public RestIntegrityService() {
        this.model = IntegrityServiceManager.getIntegrityModel();
        this.workflowManager = IntegrityServiceManager.getWorkflowManager();
        this.integrityReportProvider = IntegrityServiceManager.getIntegrityReportProvider();
    }

    /**
     * Method to get the checksum errors per pillar in a given collection.
     *
     * @param collectionID, the collectionID from which to return checksum errors
     * @param pillarID,     the ID of the pillar in the collection from which to return checksum errors
     * @param pageNumber,   the page number for calculating offsets (@see pageSize)
     * @param pageSize,     the number of checksum errors per page.
     */
    @GET
    @Path("/getChecksumErrorFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamingOutput getChecksumErrors(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID,
            @QueryParam("pageNumber")
                    int pageNumber,
            @DefaultValue("100")
            @QueryParam("pageSize")
                    int pageSize) {

        int firstID = (pageNumber - 1) * pageSize;

        return streamPartFromLatestReport(ReportPart.CHECKSUM_ISSUE, collectionID, pillarID, firstID, pageSize);
    }

    /**
     * Method to get the list of missing files per pillar in a given collection.
     *
     * @param collectionID, the collectionID from which to return missing files
     * @param pillarID,     the ID of the pillar in the collection from which to return missing files
     * @param pageNumber,   the page number for calculating offsets (@see pageSize)
     * @param pageSize,     the number of checksum errors per page.
     */
    @GET
    @Path("/getMissingFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamingOutput getMissingFileIDs(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID,
            @QueryParam("pageNumber")
                    int pageNumber,
            @DefaultValue("100")
            @QueryParam("pageSize")
                    int pageSize) {

        int firstID = (pageNumber - 1) * pageSize;

        return streamPartFromLatestReport(ReportPart.MISSING_FILE, collectionID, pillarID, firstID, pageSize);
    }

    /**
     * Method to get the list of missing checksums per pillar in a given collection.
     *
     * @param collectionID, the collectionID
     * @param pillarID,     the ID of the pillar in the collection
     * @param pageNumber,   the page number for calculating offsets (@see pageSize)
     * @param pageSize,     the maximum number of results per page.
     */
    @GET
    @Path("/getMissingChecksumsFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamingOutput getMissingChecksums(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID,
            @QueryParam("pageNumber")
                    int pageNumber,
            @DefaultValue("100")
            @QueryParam("pageSize")
                    int pageSize) {

        int firstID = (pageNumber - 1) * pageSize;

        return streamPartFromLatestReport(ReportPart.MISSING_CHECKSUM, collectionID, pillarID, firstID, pageSize);
    }

    @GET
    @Path("/getAllMissingFilesInformation/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAllMissingFilesInformation(
            @QueryParam("collectionID")
                    String collectionID) {

        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        HashMap<String, List<String>> output = new HashMap<>();
        for (String pillar : pillars) {
            //List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.MISSING_FILE, collectionID, pillar, 0,
            //        Integer.MAX_VALUE);
            //output.put(pillar, streamingOutput);

            // FOR TESTING TODO: Remove
            if (pillar.equals("file1-pillar")) {
                List<String> items1 = new ArrayList<>();
                for (int i = 2; i < 89; i++) {
                    items1.add("test-file" + i);
                }
                output.put(pillar, items1);
            } else {
                List<String> items2 = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    items2.add("test-file" + i);
                }
                output.put(pillar, items2);
            }
        }


        return output;
    }

    /**
     * Method to get the list of obsolete checksums per pillar in a given collection.
     *
     * @param collectionID, the collectionID
     * @param pillarID,     the ID of the pillar in the collection
     * @param pageNumber,   the page number for calculating offsets (@see pageSize)
     * @param pageSize,     the maximum number of results per page.
     */
    @GET
    @Path("/getObsoleteChecksumsFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamingOutput geObsoleteChecksums(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID,
            @QueryParam("pageNumber")
                    int pageNumber,
            @DefaultValue("100")
            @QueryParam("pageSize")
                    int pageSize) {

        int firstID = (pageNumber - 1) * pageSize;

        return streamPartFromLatestReport(ReportPart.OBSOLETE_CHECKSUM, collectionID, pillarID, firstID, pageSize);
    }

    /**
     * Method to get the list of present files on a pillar in a given collection.
     *
     * @param collectionID, the collectionID from which to return present file list
     * @param pillarID,     the ID of the pillar in the collection from which to return present file list
     * @param pageNumber,   the page number for calculating offsets (@see pageSize)
     * @param pageSize,     the number of checksum errors per page.
     */
    @GET
    @Path("/getAllFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamingOutput getAllFileIDs(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID,
            @QueryParam("pageNumber")
                    int pageNumber,
            @DefaultValue("100")
            @QueryParam("pageSize")
                    int pageSize) {

        int firstID = (pageNumber - 1) * pageSize;

        IntegrityIssueIterator it = model.getFilesOnPillar(pillarID, firstID, pageSize, collectionID);

        if (it != null) {
            return JSONStreamingTools.StreamIntegrityIssues(it);
        } else {
            throw new WebApplicationException(
                    Response.status(Response.Status.NO_CONTENT).entity("Failed to get missing files from database")
                            .type(MediaType.TEXT_PLAIN).build());
        }
    }

    /**
     * Get the listing of integrity status as a JSON array
     */
    @GET
    @Path("/getIntegrityStatus/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIntegrityStatus(
            @QueryParam("collectionID")
                    String collectionID) throws IOException {
        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator jg = jf.createGenerator(writer);
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        Map<String, PillarCollectionStat> stats = new HashMap<>();
        for (PillarCollectionStat stat : model.getLatestPillarStats(collectionID)) {
            if (pillars.contains(stat.getPillarID())) {
                stats.put(stat.getPillarID(), stat);
            }
        }
        for (String pillar : pillars) {
            if (!stats.containsKey(pillar)) {
                String pillarHostname = Objects.requireNonNullElse(SettingsUtils.getHostname(pillar), "N/A");
                PillarType pillarTypeObject = SettingsUtils.getPillarType(pillar);
                String pillarType = pillarTypeObject != null ? pillarTypeObject.value() : null;
                PillarCollectionStat emptyStat = new PillarCollectionStat(pillar, collectionID, pillarHostname, pillarType, 0L, 0L, 0L, 0L,
                        0L, 0L, new Date(0), new Date(0));
                stats.put(pillar, emptyStat);
            }
        }
        jg.writeStartArray();
        for (PillarCollectionStat stat : stats.values()) {
            writeIntegrityStatusObject(stat, jg);
            log.debug("IntegrityStatus: Wrote hostname: " + stat.getPillarHostname() + " to pillar" + stat.getPillarID());
        }
        jg.writeEndArray();
        jg.flush();
        writer.flush();
        return writer.toString();
    }

    /***
     * Get the current workflows setup as a JSON array
     */
    @GET
    @Path("/getWorkflowSetup/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getWorkflowSetup(
            @QueryParam("collectionID")
                    String collectionID) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            JsonFactory jf = new JsonFactory();
            JsonGenerator jg = jf.createGenerator(writer);
            jg.writeStartArray();
            for (JobID workflowID : workflowManager.getWorkflows(collectionID)) {
                writeWorkflowSetupObject(workflowID, jg);
            }
            jg.writeEndArray();
            jg.flush();
            writer.flush();
            return writer.toString();
        } catch (RuntimeException e) {
            log.error("Failed to getWorkflowSetup ", e);
            throw e;
        }
    }

    /**
     * Get the list of possible workflows as a JSON array
     */
    @GET
    @Path("/getWorkflowList/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getWorkflowList(
            @QueryParam("collectionID")
                    String collectionID) {
        List<String> workflowIDs = new ArrayList<>();
        for (JobID workflowID : workflowManager.getWorkflows(collectionID)) {
            workflowIDs.add(workflowID.getWorkflowName());
        }
        return workflowIDs;
    }

    /**
     * Get the latest integrity report, or an error message telling no such report found.
     */
    @GET
    @Path("/getLatestIntegrityReport/")
    @Produces(MediaType.TEXT_PLAIN)
    public StreamingOutput getLatestIntegrityReport(
            @QueryParam("collectionID")
                    String collectionID) {
        final File fullReport;
        try {
            fullReport = integrityReportProvider.getLatestIntegrityReportReader(collectionID).getFullReport();
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND).entity("No integrity report for collection: " + collectionID + " found!")
                            .type(MediaType.TEXT_PLAIN).build());
        }
        return output -> {
            try {
                int i;
                byte[] data = new byte[4096];
                FileInputStream is = new FileInputStream(fullReport);
                while ((i = is.read(data)) >= 0) {
                    output.write(data, 0, i);
                }
                is.close();
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };
    }

    /**
     * Start a named workflow.
     */
    @POST
    @Path("/startWorkflow/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public String startWorkflow(
            @FormParam("workflowID")
                    String workflowID,
            @FormParam("collectionID")
                    String collectionID) {
        log.debug("Starting workflow '" + workflowID + "' on collection '" + collectionID + "'.");
        return workflowManager.startWorkflow(new JobID(workflowID, collectionID));
    }

    /**
     * Start a named workflow.
     */
    @GET
    @Path("/getCollectionInformation/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCollectionInformation(
            @QueryParam("collectionID")
                    String collectionID) throws IOException {
        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator jg = jf.createGenerator(writer);
        List<CollectionStat> stats = model.getLatestCollectionStat(collectionID, 1);
        Date lastIngest = model.getDateForNewestFileEntryForCollection(collectionID);
        String lastIngestStr = lastIngest == null ? "No files ingested yet" : TimeUtils.shortDate(lastIngest);
        Long collectionSize;
        Long numberOfFiles;
        if (stats == null || stats.isEmpty()) {
            collectionSize = 0L;
            numberOfFiles = 0L;
        } else {
            CollectionStat stat = stats.get(0);
            collectionSize = stat.getDataSize();
            numberOfFiles = stat.getFileCount();
        }
        jg.writeStartObject();
        jg.writeObjectField("lastIngest", lastIngestStr);
        jg.writeObjectField("collectionSize", FileSizeUtils.toHumanShortDecimal(collectionSize));
        jg.writeObjectField("numberOfFiles", numberOfFiles);
        jg.writeEndObject();
        jg.flush();
        writer.flush();
        return writer.toString();
    }

    /**
     * Private method to help stream parts from a given ReportPart, collection and pillar.
     *
     * @param part         The part to stream issues from
     * @param collectionID The ID of the collection
     * @param pillarID     The ID of the pillar
     * @param firstID      Index of the first result
     * @param maxLines     The maximum number of lines to stream
     */
    private StreamingOutput streamPartFromLatestReport(ReportPart part, String collectionID, String pillarID, int firstID, int maxLines) {
        try {
            IntegrityReportReader reader = integrityReportProvider.getLatestIntegrityReportReader(collectionID);
            File reportPart = reader.getReportPart(part.getPartName(), pillarID);
            return JSONStreamingTools.StreamFileParts(reportPart, firstID, maxLines);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No integrity '" + part.getHumanString() + "' report part for collection: " + collectionID + " and pillar: " +
                            pillarID + " found!").type(MediaType.TEXT_PLAIN).build());
        }
    }

    private List<String> streamPartFromLatestReportNoError(ReportPart part, String collectionID, String pillarID, int firstID,
                                                           int maxLines) {
        try {
            IntegrityReportReader reader = integrityReportProvider.getLatestIntegrityReportReader(collectionID);
            File reportPart = reader.getReportPart(part.getPartName(), pillarID);
            return List.of(JSONStreamingTools.StreamFileParts(reportPart, firstID, maxLines).toString());
        } catch (FileNotFoundException e) {
            return List.of();
        }
    }

    private void writeIntegrityStatusObject(PillarCollectionStat stat, JsonGenerator jg) throws IOException {
        jg.writeStartObject();
        jg.writeObjectField("pillarID", stat.getPillarID());
        jg.writeObjectField("pillarHostname", stat.getPillarHostname());
        jg.writeObjectField("pillarType", stat.getPillarType());
        jg.writeObjectField("totalFileCount", stat.getFileCount());
        jg.writeObjectField("missingFilesCount", stat.getMissingFiles());
        jg.writeObjectField("checksumErrorCount", stat.getChecksumErrors());
        jg.writeObjectField("obsoleteChecksumsCount", stat.getObsoleteChecksums());
        jg.writeObjectField("missingChecksumsCount", stat.getMissingChecksums());
        jg.writeEndObject();
    }

    private void writeWorkflowSetupObject(JobID workflowID, JsonGenerator jg) throws IOException {
        Workflow workflow = workflowManager.getWorkflow(workflowID);
        WorkflowStatistic lastRunStatistic = workflowManager.getLastCompleteStatistics(workflowID);
        jg.writeStartObject();
        jg.writeObjectField("workflowID", workflowID.getWorkflowName());
        jg.writeObjectField("workflowDescription", workflow.getDescription());
        Date nextScheduledRun = workflowManager.getNextScheduledRun(workflowID);
        if (nextScheduledRun == null) {
            jg.writeObjectField("nextRun", "Must be run manually");
        } else {
            jg.writeObjectField("nextRun", TimeUtils.shortDate(nextScheduledRun));
        }
        if (lastRunStatistic == null) {
            jg.writeObjectField("lastRun", "Workflow hasn't finished a run yet");
            jg.writeObjectField("lastRunDetails", "Workflow hasn't finished a run yet");
            jg.writeObjectField("lastRunFinishState", "Pending");
        } else {
            jg.writeObjectField("lastRun", TimeUtils.shortDate(lastRunStatistic.getFinish()));
            jg.writeObjectField("lastRunDetails", lastRunStatistic.getFullStatistics());
            jg.writeObjectField("lastRunFinishState", lastRunStatistic.getFinishState().toString());
        }
        long runInterval = workflowManager.getRunInterval(workflowID);
        String intervalString;
        if (runInterval == -1) {
            intervalString = "Never";
        } else {
            intervalString = TimeUtils.millisecondsToHuman(runInterval);
        }
        jg.writeObjectField("executionInterval", intervalString);
        jg.writeObjectField("currentState", workflowManager.getCurrentStatistics(workflowID).getPartStatistics());
        jg.writeEndObject();
    }
}
