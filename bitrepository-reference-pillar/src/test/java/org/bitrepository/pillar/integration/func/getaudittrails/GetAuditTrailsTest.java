/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.pillar.integration.func.getaudittrails;

import java.util.List;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.testng.annotations.Test;

public class GetAuditTrailsTest extends PillarFunctionTest {
    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        settingsForTestClient.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().clear();
        settingsForTestClient.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().add(getPillarID());
    }
    
    @Test ( groups = {"pillar-integration-test"} )
    public void eventSortingTest() throws NegativeResponseException{
        addDescription("Test whether the checksum result is sorted oldest to newest.");
        addFixtureSetup("Ensure at least two files are present on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Retrieve a list of all audit trails.",
            "Run through the list and verify each element sequence number is lower than the following elements.");

        List<AuditTrailEvent> auditTrailEvents = getAuditTrails(null, null);
        
        for (int counter = 0 ; counter < auditTrailEvents.size() - 1 ; counter ++) {
            Assert.assertTrue(auditTrailEvents.get(counter).getSequenceNumber().compareTo(
                    auditTrailEvents.get(counter + 1).getSequenceNumber()) < 0,
                    "Event (" + counter + ") " + auditTrailEvents.get(counter) + " has higher sequence number" +
                            " than following event(" + counter + ") " + auditTrailEvents.get(counter + 1));
        }
    }

    @Test ( groups = {"pillar-integration-test"} )
    public void maxNumberOfResultTest() {
        addDescription("Verifies the size of the result set can be limited by setting the maxNumberOfResult parameter.");
        addFixtureSetup("Ensure at least two files are present on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        
        addStep("Retrieve a list of all audittrails by setting maxSequece to null.",
                "At 2 audit trails should be returned");
        List<AuditTrailEvent> originalAuditTrailEventList = getAuditTrails(null, null);
        
        addStep("Repeat the audit trail request, this time with maxNumberOfResult set to one",
                "A result with a single audit event should be returned. The event should be the first " +
                        "audit event in the full list.");
        AuditTrailQuery singleEventQuery = new AuditTrailQuery(getPillarID(), null, null, 1);
        List<AuditTrailEvent> singleEventList = getAuditTrails(singleEventQuery, null);
        Assert.assertEquals(singleEventList.size(), 1, "The result didn't contain a single event");
        Assert.assertEquals(singleEventList.get(0), originalAuditTrailEventList.get(0),
                "The returned event wasn't equal to the first event");
    }
    
    @Test ( groups = {"pillar-integration-test"} )
    public void minSequenceNumberTest() {
        addDescription("Test the pillar support for only retrieving events with sequence number higher than the " +
                "provided MinSequenceNumber" +
                ". " +
                "Note that this test assumes there is at least 2 audit event.");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        
        addStep("Request audit trails for all files on the pillar",
                "A list with at least 2 events is returned.");
        List<AuditTrailEvent> originalAuditTrailEventList = getAuditTrails(null, null);
        Assert.assertTrue(originalAuditTrailEventList.size() > 1,
                "The size of the returned list is only " + originalAuditTrailEventList.size() + ", " +
                        "should be at least 2");
        
        addStep("Request audit events with MinSequenceNumber set to the SequenceNumber of the first event checksum",
                "The full list of audit events should be returned.");
        int smallestSequenceNumber = originalAuditTrailEventList.get(0).getSequenceNumber().intValue();
        AuditTrailQuery firstSequenceNumberQuery = new AuditTrailQuery(getPillarID(),
                new Integer(smallestSequenceNumber), null, null);
        List<AuditTrailEvent> limitedEventList = getAuditTrails(firstSequenceNumberQuery, null);
        Assert.assertEquals(limitedEventList, originalAuditTrailEventList,
                "Different list return when MinSequenceNumber set to first event");
        
        addStep("Request audit trail with MinSequenceNumber set to the SequenceNumber of the last event",
                "Only the last event is returned.");
        int largestSequenceNumber = originalAuditTrailEventList.get(originalAuditTrailEventList.size()-1)
                .getSequenceNumber().intValue();
        AuditTrailQuery lastSequenceNumberQuery = new AuditTrailQuery(getPillarID(),
                new Integer(largestSequenceNumber), null, null);
        limitedEventList = getAuditTrails(lastSequenceNumberQuery, null);
        Assert.assertTrue(limitedEventList.size() == 1, "Received list with size of " + limitedEventList.size() + " " +
                "when requesting audit trail with MinSequenceNumber set to latest event");
        Assert.assertTrue(limitedEventList.get(0).equals(originalAuditTrailEventList.get(originalAuditTrailEventList.size()-1)),
                "The single event in audit trail result for MinSequenceNumber set to latest are different." +
                        limitedEventList);
        
        addStep("Request audit trail with MinSequenceNumber set to the SequenceNumber of the last event + 1",
                "No events are returned.");
        AuditTrailQuery laterThanLastSequenceNumberQuery = new AuditTrailQuery(getPillarID(),
                new Integer(largestSequenceNumber + 1), null, null);
        limitedEventList = getAuditTrails(laterThanLastSequenceNumberQuery, null);
        Assert.assertEmpty(limitedEventList,
                "Non-empty event list returned with laterThanLastSequenceNumberQuery: " + limitedEventList);
    }
    
    @Test ( groups = {"pillar-integration-test"} )
    public void maxSequenceNumberTest() {
        addDescription("Test the pillar support for only retrieving audit event with SequenceNumbers lower than " +
                "MaxSequenceNumber.");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request audit trails for all files on the pillar",
                "A list with at least 2 events is returned.");
        List<AuditTrailEvent> originalAuditTrailEventList = getAuditTrails(null, null);
        Assert.assertTrue(originalAuditTrailEventList.size() > 1,
                "The size of the returned list is only " + originalAuditTrailEventList.size() + ", " +
                        "should be at least 2");

        addStep("Request audit events with MaxSequenceNumber set to the SequenceNumber of the last event checksum",
                "The full list of audit events should be returned.");
        int largestSequenceNumber = originalAuditTrailEventList.get(originalAuditTrailEventList.size()-1)
                .getSequenceNumber().intValue();
        AuditTrailQuery lastSequenceNumberQuery = new AuditTrailQuery(getPillarID(),
                null, new Integer(largestSequenceNumber), null);
        List<AuditTrailEvent> limitedEventList = getAuditTrails(lastSequenceNumberQuery, null);
        Assert.assertEquals(limitedEventList, originalAuditTrailEventList,
                "Different list return when MaxSequenceNumber set to last event");

        addStep("Request audit trail with MaxSequenceNumber set to the SequenceNumber of the first event",
                "Only the first event is returned.");
        int smallestSequenceNumber = originalAuditTrailEventList.get(0).getSequenceNumber().intValue();
        AuditTrailQuery firstSequenceNumberQuery = new AuditTrailQuery(getPillarID(),
                null, new Integer(smallestSequenceNumber), null);
        limitedEventList = getAuditTrails(firstSequenceNumberQuery, null);
        Assert.assertTrue(limitedEventList.size() == 1, "Received list with size of " + limitedEventList.size() + " " +
                "when requesting audit trail with MaxSequenceNumber set to first event (expected 1 event)");
        Assert.assertEquals(limitedEventList.get(0),
                originalAuditTrailEventList.get(0),
                "Different events in the set of first events.");
    }

    private List<AuditTrailEvent> getAuditTrails(
            AuditTrailQuery componentQuery,
            String fileID) {
        List<ContributorEvent> contributorList = null;
        AuditTrailQuery[] auditTrailQueries = null;
        if (componentQuery != null) {
            auditTrailQueries = new AuditTrailQuery[] {componentQuery};
        }
        try {
            contributorList = clientProvider.getAuditTrailsClient().
                    getAuditTrails(auditTrailQueries, fileID, null, null, null);
        } catch (NegativeResponseException e) {
            throw new RuntimeException(e);
        }
        return ((AuditTrailResult)contributorList.get(0)).
                getAuditTrailEvents().getAuditTrailEvents().getAuditTrailEvent();

    }
}
