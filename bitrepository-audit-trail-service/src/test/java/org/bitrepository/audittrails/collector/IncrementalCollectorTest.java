/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.collector;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.AlarmDispatcher;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IncrementalCollectorTest extends ExtendedTestCase{
    
    public static final String TEST_COLLECTION = "dummy-collection";
    public static final String TEST_CONTRIBUTOR1 = "Contributor1";
    public static final String TEST_CONTRIBUTOR2 = "Contributor2";
    
    @Test(groups = {"regressiontest"})
    public void singleIncrementTest() throws InterruptedException {
        addDescription("Verifies the behaviour in the simplest case with just one result set ");
        AuditTrailClient client = mock(AuditTrailClient.class);
        AuditTrailStore store = mock(AuditTrailStore.class);

        AlarmDispatcher alarmDispatcher = mock(AlarmDispatcher.class);
        
        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector(TEST_COLLECTION, "Client1", client, store,
                BigInteger.ONE, alarmDispatcher);
        Collection<String> contributors = Arrays.asList("Contributor1", "Contributors2");
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));
        verify(store, timeout(3000).times(contributors.size()))
            .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));

        addStep("Send a audit trail result from contributor 1", "A AddAuditTrails call should be made to the store");
        EventHandler eventHandler = eventHandlerCaptor.getValue();
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR1, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR1, new BigInteger("1")), false));
        
        addStep("Send a audit trail result from contributor 2", "A AddAuditTrails call should be made to the " +
                "store, and the collector should finish");
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR2, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR2, new BigInteger("1")), false));
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        verify(store, timeout(3000).times(contributors.size()))
        .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));
        
        Thread.sleep(100);
        
        verify(store, timeout(3000).times(1))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR1));
        verify(store, timeout(3000).times(1))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR2));
        
        Assert.assertTrue(collectionRunner.finished, "The collector should have finished after the complete event, as " +
            "no partialResults where received");
        verifyNoMoreInteractions(store);
        verifyNoMoreInteractions(client);
        verifyZeroInteractions(alarmDispatcher);
    }

    @Test(groups = {"regressiontest"})
    public void multipleIncrementTest() throws Exception {
        addDescription("Verifies the behaviour in the case where the adit trails needs to be reteived in multiple " +
            "requests because of MaxNumberOfResults limits.");
        AuditTrailClient client = mock(AuditTrailClient.class);
        AuditTrailStore store = mock(AuditTrailStore.class);
        
        long callsToLargestSequenceNumber = 0L;
        when(store.largestSequenceNumber(any(String.class), eq(TEST_COLLECTION)))
            .thenReturn(callsToLargestSequenceNumber++);
        
        AlarmDispatcher alarmDispatcher = mock(AlarmDispatcher.class);

        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector("dummy-collection", "Client1", client, store,
                BigInteger.ONE, alarmDispatcher);
        Collection<String> contributors = Arrays.asList(TEST_CONTRIBUTOR1, TEST_CONTRIBUTOR2);
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();
     
        verify(store, timeout(3000).times(contributors.size()))
            .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));
        EventHandler eventHandler = eventHandlerCaptor.getValue();
        
        addStep("Send a audit trail result from contributor 1 and 2 with the PartialResults boolean set to true",
            "Two AddAuditTrails calls should be made, but the collector should not have finished");
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR1, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR1, new BigInteger("1")), true));
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR2, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR2, new BigInteger("1")), true));
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        verify(store, timeout(3000).times(1))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR1));
        verify(store, timeout(3000).times(1))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR2));
        
        Assert.assertTrue(!collectionRunner.finished, "The collector should not have finished after the complete " +
            "event, as partialResults where received");

        addStep("Send another audit trail result from the contributors, now with PartialResults set to false",
            "Two more AddAuditTrails calls should be made and the collector should finished");
        
        verify(store, timeout(3000).times(contributors.size() * 2))
            .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));
        
        verify(client, timeout(3000).times(2)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));
        eventHandler = eventHandlerCaptor.getValue();
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR1, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR1, new BigInteger("2")), false));
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR2, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR2, new BigInteger("2")), false));
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        verify(store, timeout(3000).times(2))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR1));
        verify(store, timeout(3000).times(2))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR2));

        Thread.sleep(100);
        Assert.assertTrue(collectionRunner.finished, "The collector should have finished after the complete event, as " +
            "no partialResults where received in the second increment.");
        
        verifyNoMoreInteractions(store);
        verifyNoMoreInteractions(client);
        verifyZeroInteractions(alarmDispatcher);
    }

    @Test(groups = {"regressiontest"})
    public void contributorFailureTest() throws Exception {
        addDescription("Tests that the collector is able to collect from the remaining contributors if a " +
            "contributor fails.");

        addStep("", "");
        AuditTrailClient client = mock(AuditTrailClient.class);
        AuditTrailStore store = mock(AuditTrailStore.class);
        
        long callsToLargestSequenceNumber = 0L;
        when(store.largestSequenceNumber(any(String.class), eq(TEST_COLLECTION)))
            .thenReturn(callsToLargestSequenceNumber++);
        
        AlarmDispatcher alarmDispatcher = mock(AlarmDispatcher.class);

        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector("dummy-collection", "Client1", client, store,
                BigInteger.ONE, alarmDispatcher);
        Collection<String> contributors = Arrays.asList(TEST_CONTRIBUTOR1, TEST_CONTRIBUTOR2);
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));
        
        verify(store, timeout(3000).times(contributors.size()))
            .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));
        
        addStep("Send a audit trail result from contributor 2 with the PartialResults boolean set to true " +
            "and a failed event from contributor 1",
            "Only one AddAuditTrails calls should be made, and the collector should not have finished");
        EventHandler eventHandler = eventHandlerCaptor.getValue();
        eventHandler.handleEvent(new ContributorFailedEvent(TEST_CONTRIBUTOR1, TEST_COLLECTION, ResponseCode.REQUEST_NOT_SUPPORTED));
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR2, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR2, new BigInteger("1")), true));
        
        verify(store, timeout(3000).times(1))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR2));
        
        eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "", null));
        Assert.assertFalse(collectionRunner.finished, "The collector should not have finished after the complete " +
            "event, as partialResults where received");

        addStep("Send another audit trail result from contributor 2 with PartialResults set to false",
            "One more AddAuditTrails calls should be made and the collector should finished");
        verify(client, timeout(3000).times(2)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));
        eventHandler = eventHandlerCaptor.getValue();
        
        verify(store, timeout(3000).times(contributors.size() + 1))
            .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));
        
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR2, TEST_COLLECTION, 
                getResultingAuditTrailsWithSingleAudit(TEST_CONTRIBUTOR2, new BigInteger("2")), false));
        
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        verify(store, timeout(3000).times(2))
            .addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(TEST_CONTRIBUTOR2));
        verify(alarmDispatcher, timeout(3000)).error(any(Alarm.class));
        
        Assert.assertTrue(collectionRunner.finished);
        verifyNoMoreInteractions(store);
        verifyNoMoreInteractions(client);
    }

    @Test(groups = {"regressiontest"})
    public void collectionIDFailureTest() throws Exception {
        addDescription("Tests what happens when a wrong collection id is received.");
        String FALSE_COLLECTION = "FalseCollection" + new Date().getTime();

        addStep("", "");
        AuditTrailClient client = mock(AuditTrailClient.class);
        AuditTrailStore store = mock(AuditTrailStore.class);
        
        long callsToLargestSequenceNumber = 0L;
        when(store.largestSequenceNumber(any(String.class), eq(TEST_COLLECTION)))
            .thenReturn(callsToLargestSequenceNumber++);
        
        AlarmDispatcher alarmDispatcher = mock(AlarmDispatcher.class);

        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector(TEST_COLLECTION, "Client1", client, store,
                BigInteger.ONE, alarmDispatcher);
        Collection<String> contributors = Arrays.asList(TEST_CONTRIBUTOR1, TEST_CONTRIBUTOR2);
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();
        Thread.sleep(100);

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));

        addStep("Send an auditTrail result from contributor 1 with a wrong collection id.",
                "It is not added to the audit store");
        EventHandler eventHandler = eventHandlerCaptor.getValue();
        eventHandler.handleEvent(new AuditTrailResult(TEST_CONTRIBUTOR2, FALSE_COLLECTION, new ResultingAuditTrails(), 
                true));
        
        verify(store, timeout(3000).times(contributors.size()))
            .largestSequenceNumber(any(String.class), eq(TEST_COLLECTION));
        
        Thread.sleep(100);
        verifyZeroInteractions(alarmDispatcher);
        verifyNoMoreInteractions(store);
    }
    
    private ResultingAuditTrails getResultingAuditTrailsWithSingleAudit(String contributor, BigInteger seq) {
        ResultingAuditTrails rats = new ResultingAuditTrails();
        AuditTrailEvents ates = new AuditTrailEvents();
        ates.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.OTHER, 
                "actor", "auditInfo", "fileID", "info", contributor, seq, "1234", "abab"));
        rats.setAuditTrailEvents(ates);
        return rats;
    }
    
    private AuditTrailEvent createSingleEvent(XMLGregorianCalendar datetime, FileAction action, String actor, 
            String auditInfo, String fileID, String info, String component, BigInteger seqNumber, String operationID,
            String fingerprint) {
        AuditTrailEvent res = new AuditTrailEvent();
        res.setActionDateTime(datetime);
        res.setActionOnFile(action);
        res.setActorOnFile(actor);
        res.setAuditTrailInformation(auditInfo);
        res.setFileID(fileID);
        res.setInfo(info);
        res.setReportingComponent(component);
        res.setSequenceNumber(seqNumber);
        res.setOperationID(operationID);
        res.setCertificateID(fingerprint);
        return res;
    }
    
    public class CollectionRunner implements Runnable {
        private final IncrementalCollector collector;
        private final Collection<String> contributors;
        boolean finished = false;

        public CollectionRunner(IncrementalCollector collector, Collection<String> contributors) {
            this.collector = collector;
            this.contributors = contributors;
        }

        public void run() {
            collector.performCollection(contributors);
            finished = true;
        }
    }
}
