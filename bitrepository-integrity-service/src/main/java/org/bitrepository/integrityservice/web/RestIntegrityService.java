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
import org.bitrepository.integrityservice.reports.IntegrityReportConstants;
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
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static javax.ws.rs.core.Response.ResponseBuilder;
import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.status;

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
     * REST endpoint to get the list of present files on a pillar in a given collection.
     *
     * @param collectionID The collection ID from which to return present file list.
     * @param pillarID     The ID of the pillar in the collection from which to return present file list
     * @return Returns a {@link HashMap} containing a key-pair of pillarID and its missing files as a {@link String} and
     * {@link List<String>}.
     */
    @GET
    @Path("/getTotalFileIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getTotalFileIDs(@QueryParam("collectionID") String collectionID,
                                                         @QueryParam("pillarID") String pillarID,
                                                         @QueryParam("page") int page,
                                                         @DefaultValue("100") @QueryParam("pageSize") int pageSize) {

        IntegrityIssueIterator it = model.getFilesOnPillar(pillarID, getOffset(page, pageSize), pageSize, collectionID);

        if (it == null) {
            throw new WebApplicationException(
                    status(Status.NO_CONTENT).entity("Failed to get missing files from database")
                            .type(MediaType.TEXT_PLAIN).build());
        }

        List<String> iteratorAsList = StreamingTools.iteratorToList(it);
        if (iteratorAsList.isEmpty()) {
            throw new WebApplicationException(status(Status.NOT_FOUND).entity(
                    String.format(Locale.ROOT, "No fileIDs found for collection: '%s' and pillar: '%s'", collectionID,
                            pillarID)).type(MediaType.TEXT_PLAIN).build());
        }

        return new HashMap<>(Map.of(pillarID, iteratorAsList));
    }

    /**
     * REST endpoint to get the list of missing files for a pillar in a given collection.
     *
     * @param collectionID The collection ID from which to return missing files.
     * @return Returns a {@link HashMap} mapping the given pillar to its missing files.
     */
    @GET
    @Path("/getMissingFileIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getMissingFileIDs(@QueryParam("collectionID") String collectionID,
                                                           @QueryParam("pillarID") String pillarID,
                                                           @QueryParam("page") int page,
                                                           @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
        HashMap<String, List<String>> output = new HashMap<>();
        ReportPart part = ReportPart.MISSING_FILE;
        List<String> missingOnPillar;
        List<String> missingOnOtherPillar;

        List<String> otherPillars = SettingsUtils.getPillarIDsForCollection(collectionID).stream()
                .filter(pillar -> !pillar.equals(pillarID)).collect(Collectors.toList());

        try {
            missingOnPillar = getReportPart(part, collectionID, pillarID, page, pageSize);
            output.put(pillarID, missingOnPillar);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(status(Status.NOT_FOUND).entity(String.format(Locale.ROOT,
                    "No integrity '%s' report part for collection: '%s' and pillar: '%s' found!", part.getHumanString(),
                    collectionID, pillarID)).type(MediaType.TEXT_PLAIN).build());
        }

        for (String otherPillar : otherPillars) {
            missingOnOtherPillar = compareMissingFiles(missingOnPillar, collectionID, otherPillar);
            output.put(otherPillar, missingOnOtherPillar);
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
    @Path("/getMissingChecksumsFileIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getMissingChecksums(@QueryParam("collectionID") String collectionID,
                                                             @QueryParam("pillarID") String pillarID,
                                                             @QueryParam("page") int page,
                                                             @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
        List<String> streamingOutput = getReportPartForPillar(ReportPart.MISSING_CHECKSUM, collectionID, pillarID, page,
                pageSize);
        return new HashMap<>(Map.of(pillarID, streamingOutput));
    }

    /**
     * REST endpoint to get the list of obsolete checksums for a pillar in a given collection.
     *
     * @param collectionID The collection ID.
     * @param pillarID     The ID of the pillar in the collection
     * @return Returns a {@link HashMap} mapping the given pillar to its obsolete checksums.
     */
    @GET
    @Path("/getObsoleteChecksumsFileIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> geObsoleteChecksums(@QueryParam("collectionID") String collectionID,
                                                             @QueryParam("pillarID") String pillarID,
                                                             @QueryParam("page") int page,
                                                             @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
        List<String> streamingOutput = getReportPartForPillar(ReportPart.OBSOLETE_CHECKSUM, collectionID, pillarID,
                page, pageSize);
        return new HashMap<>(Map.of(pillarID, streamingOutput));
    }

    /**
     * REST endpoint that fetches the checksum errors, that are inconsistent, for a given pillar in a collection.
     *
     * @param collectionID The collectionID from which to return checksum errors
     * @param pillarID     The ID of the pillar in the collection from which to return checksum errors
     * @return Returns a {@link HashMap} mapping the given pillar to the checksum errors that are inconsistent.
     */
    @GET
    @Path("/getChecksumErrorFileIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getChecksumErrors(@QueryParam("collectionID") String collectionID,
                                                           @QueryParam("pillarID") String pillarID,
                                                           @QueryParam("page") int page,
                                                           @DefaultValue("100") @QueryParam("pageSize") int pageSize) {
        List<String> streamingOutput = getReportPartForPillar(ReportPart.CHECKSUM_ERROR, collectionID, pillarID, page,
                pageSize);
        return new HashMap<>(Map.of(pillarID, streamingOutput));
    }

    /**
     * Get the listing of integrity status as a JSON array
     */
    @GET
    @Path("/getIntegrityStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIntegrityStatus(@QueryParam("collectionID") String collectionID) throws IOException {
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
                String pillarName = Objects.requireNonNullElse(SettingsUtils.getPillarName(pillar), "N/A");
                PillarType pillarTypeObject = SettingsUtils.getPillarType(pillar);
                String pillarType = pillarTypeObject != null ? pillarTypeObject.value() : null;
                PillarCollectionStat emptyStat = new PillarCollectionStat(pillar, collectionID, pillarName, pillarType,
                        0L, 0L, 0L, 0L, 0L, 0L, "", null, new Date(0), new Date(0));
                stats.put(pillar, emptyStat);
            }
        }
        jg.writeStartArray();
        for (PillarCollectionStat stat : stats.values()) {
            writeIntegrityStatusObject(stat, jg);
            log.debug("IntegrityStatus: Wrote pillar name: '{}' to pillar '{}'", stat.getPillarName(),
                    stat.getPillarID());
        }
        jg.writeEndArray();
        jg.flush();
        writer.flush();
        return writer.toString();
    }

    /***
     * Get the current workflow’s setup as a JSON array
     */
    @GET
    @Path("/getWorkflowSetup")
    @Produces(MediaType.APPLICATION_JSON)
    public String getWorkflowSetup(@QueryParam("collectionID") String collectionID) throws IOException {
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
    @Path("/getWorkflowList")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getWorkflowList(@QueryParam("collectionID") String collectionID) {
        List<String> workflowIDs = new ArrayList<>();
        for (JobID workflowID : workflowManager.getWorkflows(collectionID)) {
            workflowIDs.add(workflowID.getWorkflowName());
        }
        return workflowIDs;
    }

    /**
     * Gets a {@link HashMap} with key {@link String} representing the {@link ReportPart} and value {@link List} of {@link String}s
     * representing
     * which pillars have a file for the given report part.
     * {@link ReportPart}
     *
     * @param collectionID The collection ID for which the look at.
     * @return {@link HashMap} mapping {@link ReportPart} to a {@link List} of pillars.
     */
    @GET
    @Path("/getAvailableIntegrityReports")
    @Produces(MediaType.APPLICATION_JSON)
    public HashMap<String, List<String>> getAvailableIntegrityReports(@QueryParam("collectionID") String collectionID) {
        HashMap<String, List<String>> availableIntegrityReports = new HashMap<>();
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        Set<ReportPart> reportParts = IntegrityReportConstants.getReportParts();

        for (String pillarID : pillars) {
            List<String> reportPartOnPillar = new ArrayList<>();
            for (ReportPart currentReportPart : reportParts) {
                try {
                    getReportPart(currentReportPart, collectionID, pillarID, 0, Integer.MAX_VALUE);
                    reportPartOnPillar.add(currentReportPart.getPartName());
                } catch (FileNotFoundException e) {
                    log.debug(e.getMessage());
                }
            }
            availableIntegrityReports.put(pillarID, reportPartOnPillar);
        }

        return availableIntegrityReports;
    }

    /**
     * Get the latest integrity report, or an error message telling no such report found.
     */
    @GET
    @Path("/getLatestIntegrityReport")
    @Produces(MediaType.TEXT_PLAIN)
    public StreamingOutput getLatestIntegrityReport(@QueryParam("collectionID") String collectionID) {
        final File fullReport;
        try {
            fullReport = integrityReportProvider.getLatestIntegrityReportReader(collectionID).getFullReport();
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(status(Status.NOT_FOUND).entity(
                            String.format(Locale.ROOT, "No integrity report for collection: '%s' found!", collectionID))
                    .type(MediaType.TEXT_PLAIN).build());
        }
        return output -> streamFile(fullReport, output);
    }

    @GET
    @Path("/getIntegrityReportsAsZIP")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getIntegrityReportsAsZIP(@QueryParam("collectionID") String collectionID,
                                             @QueryParam("reports") List<String> reports) {
        String fileName = "IntegrityReports.zip";
        HashMap<String, File> files = new HashMap<>();

        for (String report : reports) {
            String[] parts = report.split("-", 2);
            files.put(report, getLatestIntegrityReportPartFile(collectionID, parts[0], parts[1]));
        }

        StreamingOutput streamingOutput = output -> {
            ZipOutputStream zipOut = new ZipOutputStream(output);
            // Add the full integrity report to the hashmap
            files.put("report", integrityReportProvider.getLatestIntegrityReportReader(collectionID).getFullReport());

            // Zip each file in the files hashmap
            files.forEach((key, value) -> {
                try {
                    zipOut.putNextEntry(new ZipEntry(key));
                    zipOut.write(Files.readAllBytes(value.toPath()));
                    zipOut.flush();
                } catch (IOException e) {
                    throw new WebApplicationException(status(Status.INTERNAL_SERVER_ERROR).entity(
                                    "Something went wrong when trying to zip the file " + key + ".").type(MediaType.TEXT_PLAIN)
                            .build());
                }
            });
            zipOut.close();
        };
        ResponseBuilder response = Response.ok();
        response.type("application/zip");
        response.header("Content-Disposition", "attachment; filename=" + fileName);
        response.entity(streamingOutput);

        return response.build();
    }

    /**
     * Get the latest integrity report, or an error message telling no such report found.
     *
     * @param collectionID The collection ID.
     * @param pillarID     The pillar ID.
     * @param reportPart   The report part.
     * @return {@link StreamingOutput} of the report part for the given collection and pillar.
     */
    public File getLatestIntegrityReportPartFile(String collectionID, String reportPart, String pillarID) {
        final File reportPartFile;
        try {
            reportPartFile = integrityReportProvider.getIntegrityReportPart(collectionID, pillarID, reportPart);
        } catch (FileNotFoundException e) {
            String errorMessage = String.format(Locale.ROOT,
                    "No '%s' report part for collection: '%s' and pillar: '%s' found!", reportPart, collectionID,
                    pillarID);
            log.error(errorMessage);
            throw new WebApplicationException(
                    status(Status.NOT_FOUND).entity(errorMessage).type(MediaType.TEXT_PLAIN).build());
        }
        return reportPartFile;
    }

    /**
     * Start a named workflow.
     */
    @POST
    @Path("/startWorkflow")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public String startWorkflow(@FormParam("workflowID") String workflowID,
                                @FormParam("collectionID") String collectionID) {
        log.debug("Starting workflow '" + workflowID + "' on collection '" + collectionID + "'.");
        return workflowManager.startWorkflow(new JobID(workflowID, collectionID));
    }

    /**
     * Start a named workflow.
     */
    @GET
    @Path("/getCollectionInformation")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCollectionInformation(@QueryParam("collectionID") String collectionID) throws IOException {
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
     * Private method to help stream parts from a given {@link ReportPart}, collection and pillar.
     *
     * @param part         The part to stream issues from.
     * @param collectionID The ID of the collection.
     * @param pillarID     The ID of the pillar.
     * @return Returns a {@link List} of fileIDs from {@link ReportPart} of the given collection and pillar.
     * @throws FileNotFoundException If there is no integrity report for the given {@link ReportPart}.
     */
    private List<String> getReportPart(ReportPart part, String collectionID, String pillarID, int page, int pageSize)
            throws FileNotFoundException {
        List<String> reportPartContent;
        int offset = getOffset(page, pageSize);

        IntegrityReportReader reader = integrityReportProvider.getLatestIntegrityReportReader(collectionID);
        File reportPart = reader.getReportPart(part.getPartName(), pillarID);
        reportPartContent = StreamingTools.filePartToList(reportPart, offset, pageSize);

        return reportPartContent;
    }

    /**
     * Overloaded method calling {@link RestIntegrityService#getReportPart} but instead of returning an empty list, it will throw a
     * {@link WebApplicationException}.
     *
     * @return Returns either a {@link List<String>} of fileIDs or throws a {@link WebApplicationException}.
     */
    private List<String> getReportPartForPillar(ReportPart part, String collectionID, String pillarID, int page,
                                                int pageSize) {
        List<String> reportPartContent;
        try {
            reportPartContent = getReportPart(part, collectionID, pillarID, page, pageSize);
        } catch (FileNotFoundException e) {
            throw new WebApplicationException(status(Status.NOT_FOUND).entity(String.format(Locale.ROOT,
                    "No integrity '%s' report part for collection: '%s' and pillar: '%s' found!", part.getHumanString(),
                    collectionID, pillarID)).type(MediaType.TEXT_PLAIN).build());
        }
        return reportPartContent;
    }

    /**
     * Compares the missing fileIDs to that of the missing fileIDs of the given pillar.
     * <br>
     * It is <u><b>important</b></u> to notice, that in order for this to work, it expects all the contents of the
     * integrity reports to have been sorted using<br>
     * {@link String#compareTo(String)}.
     *
     * @param missingOnPillar The list of files that are missing on the pillar in focus.
     * @param collectionID    The collection ID.
     * @param pillar          The pillar to compare to.
     * @return Returns a {@link List<String>} containing the files that were also missing on the given pillar.
     */
    private List<String> compareMissingFiles(List<String> missingOnPillar, String collectionID, String pillar) {
        List<String> agreedMissingFileIDs = new ArrayList<>();
        for (String missingFileID : missingOnPillar) {
            try {
                if (missingOnPillar(missingFileID, collectionID, pillar)) {
                    agreedMissingFileIDs.add(missingFileID);
                }
            } catch (FileNotFoundException ignored) {
                break; // If there is no integrity report for the given pillar, we break the for-loop.
            }
        }

        return agreedMissingFileIDs;
    }

    /**
     * Helper method to found out if a {@code fileID} is also missing on another given pillar.
     *
     * @return A {@link Boolean} representing if the {@code fileID} is missing on the given pillar.
     * @throws FileNotFoundException If there's no integrity report, a {@link FileNotFoundException} will be thrown.
     */
    private boolean missingOnPillar(String fileID, String collectionID, String pillar) throws FileNotFoundException {
        List<String> filesMissingOnPillar = getReportPart(ReportPart.MISSING_FILE, collectionID, pillar, 1,
                Integer.MAX_VALUE);

        if (!filesMissingOnPillar.isEmpty()) {
            return Collections.binarySearch(filesMissingOnPillar, fileID) >= 0;
        }

        return false;
    }

    /**
     * Helper method to compute start index.
     */
    private int getOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    /**
     * Streams the given file, allowing it to be downloaded on REST api call.
     *
     * @param file   The {@link File} to stream.
     * @param output The {@link OutputStream} to write the stream to.
     */
    private void streamFile(File file, OutputStream output) {
        try {
            int i;
            byte[] data = new byte[4096];
            FileInputStream is = new FileInputStream(file);
            while ((i = is.read(data)) >= 0) {
                output.write(data, 0, i);
            }
            is.close();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private void writeIntegrityStatusObject(PillarCollectionStat stat, JsonGenerator jg) throws IOException {
        jg.writeStartObject();
        jg.writeObjectField("pillarID", stat.getPillarID());
        jg.writeObjectField("pillarName", stat.getPillarName());
        jg.writeObjectField("pillarType", stat.getPillarType());
        jg.writeObjectField("totalFileCount", stat.getFileCount());
        jg.writeObjectField("missingFilesCount", stat.getMissingFiles());
        jg.writeObjectField("checksumErrorCount", stat.getChecksumErrors());
        jg.writeObjectField("obsoleteChecksumsCount", stat.getObsoleteChecksums());
        jg.writeObjectField("missingChecksumsCount", stat.getMissingChecksums());
        jg.writeObjectField("maxAgeForChecksums", stat.getMaxAgeForChecksums());
        jg.writeObjectField("ageOfOldestChecksum", stat.getAgeOfOldestChecksum());
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
