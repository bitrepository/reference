/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice.collector;

import org.bitrepository.TestGroups;
import org.bitrepository.access.getstatus.conversation.StatusCompleteContributorEvent;
import org.bitrepository.client.eventhandler.AbstractOperationEvent;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.DefaultEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.monitoringservice.MockAlerter;
import org.bitrepository.monitoringservice.MockStatusStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;


public class StatusEventHandlerTest {

    public static final String TEST_COLLECTION = "collection1";

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testStatusEventHandler() throws Exception {
        addDescription("Test the GetStatusEventHandler handling of events");
        addStep("Setup", "");
        MockStatusStore store = new MockStatusStore();
        MockAlerter alerter = new MockAlerter();
        GetStatusEventHandler eventHandler = new GetStatusEventHandler(store, alerter);

        addStep("Validate initial calls to the mocks", "No calls expected");
        Assertions.assertEquals(0, store.getCallsForGetStatusMap());
        Assertions.assertEquals(0, store.getCallsForUpdateReplayCounts());
        Assertions.assertEquals(0, store.getCallsForUpdateStatus());
        Assertions.assertEquals(0, alerter.getCallsForCheckStatuses());

        addStep("Test an unhandled event.", "Should not make any calls.");
        AbstractOperationEvent event = new DefaultEvent(OperationEvent.OperationEventType.WARNING, TEST_COLLECTION);
        eventHandler.handleEvent(event);

        Assertions.assertEquals(0, store.getCallsForGetStatusMap());
        Assertions.assertEquals(0, store.getCallsForUpdateReplayCounts());
        Assertions.assertEquals(0, store.getCallsForUpdateStatus());
        Assertions.assertEquals(0, alerter.getCallsForCheckStatuses());

        addStep("Test the Complete event", "Should make a call to the alerter");
        event = new CompleteEvent(TEST_COLLECTION, null);
        eventHandler.handleEvent(event);
        Assertions.assertEquals(0, store.getCallsForGetStatusMap());
        Assertions.assertEquals(0, store.getCallsForUpdateReplayCounts());
        Assertions.assertEquals(0, store.getCallsForUpdateStatus());
        Assertions.assertEquals(1, alerter.getCallsForCheckStatuses());

        addStep("Test the Failed event", "Should make another call to the alerter");
        event = new OperationFailedEvent(null, "info", null);
        eventHandler.handleEvent(event);
        Assertions.assertEquals(0, store.getCallsForGetStatusMap());
        Assertions.assertEquals(0, store.getCallsForUpdateReplayCounts());
        Assertions.assertEquals(0, store.getCallsForUpdateStatus());
        Assertions.assertEquals(2, alerter.getCallsForCheckStatuses());

        addStep("Test the component complete status", "Should attempt to update the store");
        event = new StatusCompleteContributorEvent("ContributorID", "dummy-collection", null);
        eventHandler.handleEvent(event);
        Assertions.assertEquals(0, store.getCallsForGetStatusMap());
        Assertions.assertEquals(0, store.getCallsForUpdateReplayCounts());
        Assertions.assertEquals(1, store.getCallsForUpdateStatus());
        Assertions.assertEquals(2, alerter.getCallsForCheckStatuses());
    }
}
