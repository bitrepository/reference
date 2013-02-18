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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.MockAuditClient;
import org.bitrepository.audittrails.MockAuditStore;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IncrementalCollectorTest extends ExtendedTestCase{
    @Test(groups = {"regressiontest"})
    public void singleIncrementTest() throws InterruptedException {
        addDescription("Verifies the behaviour in the simplest case with just one result set ");
        MockAuditClient client = new MockAuditClient();
        MockAuditStore store = new MockAuditStore();

        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector("dummy-collection", "Client1", client, store,
                BigInteger.ONE);
        Collection<String> contributors = Arrays.asList("Contributor1", "Contributors2");
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();
        Thread.sleep(100);
        
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), client.getCallsToGetAuditTrails() 
                * contributors.size(),
                "There should be one call for largest sequence number for each contributor for each call to the client.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);

        addStep("Send a audit trail result from contributor 1", "A AddAuditTrails call should be made to the store");
        EventHandler eventHandler = client.getLatestEventHandler();
        eventHandler.handleEvent(new AuditTrailResult("Contributor1", new ResultingAuditTrails(), false));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 1,
            "Should have been just one call to store after the first result.");
        eventHandler.handleEvent(new AuditTrailResult("Contributor2", new ResultingAuditTrails(), false));
        addStep("Send a audit trail result from contributor 2", "A AddAuditTrails call should be made to the " +
            "store, and the collector should finish");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 2,
            "Should have been ecactly two calls to store after the second result.");
        eventHandler.handleEvent(new CompleteEvent(null));
        Thread.sleep(100);
        Assert.assertTrue(collectionRunner.finished, "The collector should have finished after the complete event, as " +
            "no partialResults where received");
    }

    @Test(groups = {"regressiontest"})
    public void multipleIncrementTest() throws Exception {
        addDescription("Verifies the behaviour in the case where the adit trails needs to be reteived in multiple " +
            "requests because of MaxNumberOfResults limits.");
        MockAuditClient client = new MockAuditClient();
        MockAuditStore store = new MockAuditStore();

        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector("dummy-collection", "Client1", client, store,
                BigInteger.ONE);
        Collection<String> contributors = Arrays.asList("Contributor1", "Contributors2");
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();
        Thread.sleep(100);

        Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), 2,
            "There should be one call for largest sequence number for each contributor for each call to the client.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);

        EventHandler eventHandler = client.getLatestEventHandler();
        addStep("Send a audit trail result from contributor 1 and 2 with the PartialResults boolean set to true",
            "Two AddAuditTrails calls should be made, but the collector should not have finished");
        eventHandler.handleEvent(new AuditTrailResult("Contributor1", new ResultingAuditTrails(), true));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 1);
        eventHandler.handleEvent(new AuditTrailResult("Contributor2", new ResultingAuditTrails(), true));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 2);
        eventHandler.handleEvent(new CompleteEvent(null));
        Thread.sleep(100);
        Assert.assertTrue(!collectionRunner.finished, "The collector should not have finished after the complete " +
            "event, as partialResults where received");

        addStep("Send another audit trail result from the contributors, now with PartialResults set to false",
            "Two more AddAuditTrails calls should be made and the collector should finished");
        eventHandler = client.getLatestEventHandler();
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), 4);
        eventHandler.handleEvent(new AuditTrailResult("Contributor1", new ResultingAuditTrails(), false));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 3,
            "Should have been three calls to store after the second increments, first result.");
        eventHandler.handleEvent(new AuditTrailResult("Contributor2", new ResultingAuditTrails(), false));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 4,
            "Should have been four calls to store after the second increments, second result.");
        eventHandler.handleEvent(new CompleteEvent(null));
        Thread.sleep(100);
        Assert.assertTrue(collectionRunner.finished, "The collector should have finished after the complete event, as " +
            "no partialResults where received in the second increment.");
    }

    @Test(groups = {"regressiontest"})
    public void ContributorFailureTest() throws Exception {
        addDescription("Tests that the collector is able to collect from the remaining contributors if a " +
            "contributor fails.");

        addStep("", "");
        MockAuditClient client = new MockAuditClient();
        MockAuditStore store = new MockAuditStore();

        addStep("Start a collection with two contributors", "A call should be made to the store to find out which " +
            "sequence number to continue from");
        IncrementalCollector collector = new IncrementalCollector("dummy-collection", "Client1", client, store,
                BigInteger.ONE);
        Collection<String> contributors = Arrays.asList("Contributor1", "Contributors2");
        CollectionRunner collectionRunner = new CollectionRunner(collector, contributors);
        Thread t = new Thread(collectionRunner);
        t.start();
        Thread.sleep(100);

        Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), 2,
            "There should be one call for largest sequence number for each contributor for each call to the client.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);

        addStep("Send a audit trail result from contributor 1 with the PartialResults boolean set to true " +
            "and a failed event from contributor 2",
            "Only one AddAuditTrails calls should be made, and the collector should not have finished");
        EventHandler eventHandler = client.getLatestEventHandler();
        eventHandler.handleEvent(new ContributorFailedEvent("Contributor1", ResponseCode.REQUEST_NOT_SUPPORTED));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        eventHandler.handleEvent(new AuditTrailResult("Contributor2", new ResultingAuditTrails(), true));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 1);
        eventHandler.handleEvent(new OperationFailedEvent("", null));
        Thread.sleep(100);
        Assert.assertTrue(!collectionRunner.finished, "The collector should not have finished after the complete " +
            "event, as partialResults where received");

        addStep("Send another audit trail result from contributor 1 with PartialResults set to false",
            "One more AddAuditTrails calls should be made and the collector should finished");
        eventHandler = client.getLatestEventHandler();
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), 3);
        eventHandler.handleEvent(new AuditTrailResult("Contributor1", new ResultingAuditTrails(), false));
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 2,
            "Should have been three calls to store after the second increments.");
        eventHandler.handleEvent(new CompleteEvent(null));
        Thread.sleep(100);
        Assert.assertTrue(collectionRunner.finished);
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
