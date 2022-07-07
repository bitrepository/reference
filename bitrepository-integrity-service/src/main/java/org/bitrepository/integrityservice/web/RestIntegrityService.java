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
    private List<String> pillars;

    public RestIntegrityService() {
        this.model = IntegrityServiceManager.getIntegrityModel();
        this.workflowManager = IntegrityServiceManager.getWorkflowManager();
        this.integrityReportProvider = IntegrityServiceManager.getIntegrityReportProvider();
    }

    /**
     * REST endpoint to get the list of present files on a pillar in a given collection.
     *
     * @param collectionID The collection ID from which to return present file list.
     * @param pillarID     The ID of the pillar in the collection from which to return present file list
     * @return TODO: Missing
     */
    @GET
    @Path("/getAllFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAllFileIDs(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID) {

        IntegrityIssueIterator it = model.getFilesOnPillar(pillarID, 0, Integer.MAX_VALUE, collectionID);

        List<String> fileList;
        if (it != null) {
            fileList = List.of(JSONStreamingTools.StreamIntegrityIssues(it).toString());
        } else {
            throw new WebApplicationException(
                    Response.status(Response.Status.NO_CONTENT).entity("Failed to get missing files from database")
                            .type(MediaType.TEXT_PLAIN).build());
        }

        return new HashMap<>() {{
            put(pillarID, fileList);
        }};
    }

    /**
     * REST endpoint to get the list of missing files for a pillar in a given collection.
     *
     * @param collectionID The collection ID from which to return missing files.
     * @return Returns a {@link HashMap} mapping the given pillar to its missing files.
     */
    @GET
    @Path("/getMissingFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getMissingFileIDs(
            @QueryParam("collectionID")
                    String collectionID) {
        pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        HashMap<String, List<String>> output = new HashMap<>();
        for (String pillar : pillars) {
            List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.MISSING_FILE, collectionID, pillar, 0,
                    Integer.MAX_VALUE);
            output.put(pillar, streamingOutput);

            // FOR TESTING TODO: Remove
            if (pillar.equals("file1-pillar")) {
                List<String> items1 = new ArrayList<>();
                for (int i = 1; i < 70; i++) {
                    items1.add("test-single-pillar-file" + i);
                }
                output.put(pillar, items1);
            } else {
                List<String> items2 = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    items2.add("test-single-pillar-file" + i);
                }
                output.put(pillar, items2);
            }
        }

        return output;
    }


    /**
     * REST endpoint to get the list of missing checksums for a pillar in a given collection.
     *
     * @param collectionID, The collection ID.
     * @param pillarID,     The ID of the pillar in the collection
     * @return Returns a {@link HashMap} mapping the given pillar to its missing checksums.
     */
    @GET
    @Path("/getMissingChecksumsFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getMissingChecksums(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID) {
        List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.MISSING_CHECKSUM, collectionID, pillarID, 0,
                Integer.MAX_VALUE);
        return new HashMap<>() {{
            put(pillarID, streamingOutput);
        }};
    }

    /**
     * REST endpoint to get the list of obsolete checksums for a pillar in a given collection.
     *
     * @param collectionID The collection ID.
     * @param pillarID     The ID of the pillar in the collection
     * @return Returns a {@link HashMap} mapping the given pillar to its obsolete checksums.
     */
    @GET
    @Path("/getObsoleteChecksumsFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> geObsoleteChecksums(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID) {
        List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.OBSOLETE_CHECKSUM, collectionID, pillarID, 0,
                Integer.MAX_VALUE);
        return new HashMap<>() {{
            put(pillarID, streamingOutput);
        }};
    }

    /**
     * REST endpoint that fetches the checksum errors, that are inconsistent, for a given pillar in a collection.
     *
     * @param collectionID The collectionID from which to return checksum errors
     * @param pillarID     The ID of the pillar in the collection from which to return checksum errors
     * @return Returns a {@link HashMap} mapping the given pillar to the checksum errors that are inconsistent.
     */
    @GET
    @Path("/getChecksumErrorFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getChecksumErrors(
            @QueryParam("collectionID")
                    String collectionID,
            @QueryParam("pillarID")
                    String pillarID) {
        List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.CHECKSUM_ERROR, collectionID, pillarID, 0,
                Integer.MAX_VALUE);
        return new HashMap<>() {{
            put(pillarID, streamingOutput);
        }};
    }

    /**
     * REST endpoint that fetches all missing files from all available pillars for the current collection ID.
     *
     * @param collectionID The current collection ID.
     * @return Returns a {@link HashMap} mapping each pillar ID to the files it is missing.
     */
    @GET
    @Path("/getAllMissingFilesInformation/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAllMissingFilesInformation(
            @QueryParam("collectionID")
                    String collectionID) {
        pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        HashMap<String, List<String>> output = new HashMap<>();
        for (String pillar : pillars) {
            List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.MISSING_FILE, collectionID, pillar, 0,
                    Integer.MAX_VALUE);
            output.put(pillar, streamingOutput);

            // FOR TESTING TODO: Remove
            if (pillar.equals("file1-pillar")) {
                List<String> items1 = new ArrayList<>();
                for (int i = 2; i < 899; i++) {
                    items1.add("test-file" + i);
                }
                output.put(pillar, items1);
            } else {
                List<String> items2 = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    items2.add("test-file" + i);
                }
                output.put(pillar, items2);
            }
        }

        return output;
    }

    /**
     * REST endpoint that fetches all missing checksums from all available pillars for the current collection ID.
     *
     * @param collectionID The current collection ID.
     * @return Returns a {@link HashMap} mapping each pillar ID to the checksums it is missing.
     */
    @GET
    @Path("/getAllMissingChecksumsInformation/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAllMissingChecksumsInformation(
            @QueryParam("collectionID")
                    String collectionID) {
        pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        HashMap<String, List<String>> output = new HashMap<>();
        for (String pillar : pillars) {
            List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.MISSING_CHECKSUM, collectionID, pillar, 0,
                    Integer.MAX_VALUE);
            output.put(pillar, streamingOutput);

            // FOR TESTING TODO: Remove
            if (pillar.equals("file1-pillar")) {
                List<String> items1 = new ArrayList<>();
                for (int i = 2; i < 200; i++) {
                    items1.add("missing-checksum-test-file" + i + ".zip");
                }
                output.put(pillar, items1);
            } else {
                List<String> items2 = new ArrayList<>();
                for (int i = 0; i < 206; i++) {
                    items2.add("missing-checksum-test-file" + i + ".zip");
                }
                output.put(pillar, items2);
            }
        }

        return output;
    }

    /**
     * REST endpoint that fetches all obsolete checksums from all available pillars for the current collection ID.
     *
     * @param collectionID The current collection ID.
     * @return Returns a {@link HashMap} mapping each pillar ID to its obsolete checksums.
     */
    @GET
    @Path("/getAllObsoleteChecksumsInformation/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAllObsoleteChecksumsInformation(
            @QueryParam("collectionID")
                    String collectionID) {
        pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        HashMap<String, List<String>> output = new HashMap<>();
        for (String pillar : pillars) {
            List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.OBSOLETE_CHECKSUM, collectionID, pillar, 0,
                    Integer.MAX_VALUE);
            output.put(pillar, streamingOutput);

            // FOR TESTING TODO: Remove
            if (pillar.equals("file1-pillar")) {
                List<String> items1 = new ArrayList<>();
                for (int i = 2; i < 200; i++) {
                    items1.add("obsolete-checksum-test-file" + i + ".rar");
                }
                output.put(pillar, items1);
            } else {
                List<String> items2 = new ArrayList<>();
                for (int i = 0; i < 206; i++) {
                    items2.add("obsolete-checksum-test-file" + i + ".rar");
                }
                output.put(pillar, items2);
            }
        }

        return output;
    }

    /**
     * REST endpoint that fetches all inconsistent checksums from all available pillars for the current collection ID.
     *
     * @param collectionID The current collection ID.
     * @return Returns a {@link HashMap} mapping each pillar ID to its inconsistent checksums.
     */
    @GET
    @Path("/getAllInconsistentChecksumsInformation/")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAllInconsistentChecksumsInformation(
            @QueryParam("collectionID")
                    String collectionID) {
        pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        HashMap<String, List<String>> output = new HashMap<>();
        for (String pillar : pillars) {
            List<String> streamingOutput = streamPartFromLatestReportNoError(ReportPart.CHECKSUM_ERROR, collectionID, pillar, 0,
                    Integer.MAX_VALUE);
            output.put(pillar, streamingOutput);

            // FOR TESTING TODO: Remove
            if (pillar.equals("file1-pillar")) {
                List<String> items1 = new ArrayList<>();
                for (int i = 2; i < 200; i++) {
                    items1.add("inconsistent-checksum-test-file" + i + ".txt");
                }
                output.put(pillar, items1);
            } else {
                List<String> items2 = new ArrayList<>();
                for (int i = 0; i < 206; i++) {
                    items2.add("inconsistent-checksum-test-file" + i + ".txt");
                }
                output.put(pillar, items2);
            }
        }

        return output;
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
     * Get the current workflow's setup as a JSON array
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
