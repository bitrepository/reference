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
package org.bitrepository.audittrails.preserver;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class AuditPreservationEventHandlerTest extends ExtendedTestCase {
    String PILLARID = "pillarID";
    public static final String TEST_COLLECTION = "dummy-collection";

    @Test(groups = {"regressiontest"})
    public void auditPreservationEventHandlerTest() throws Exception {
        addDescription("Test the handling of the audit trail event handler.");
        addStep("Setup", "");
        Map<String, Long> map = new HashMap<String, Long>();
        map.put(PILLARID, 1L);
        AuditTrailStore store = mock(AuditTrailStore.class);
        when(store.havePreservationKey(eq(PILLARID), eq(TEST_COLLECTION))).thenReturn(true);
        
        AuditPreservationEventHandler eventHandler = new AuditPreservationEventHandler(map, store, TEST_COLLECTION);
        
        addStep("Test the handling of another complete event.", "Should make a call");
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        verify(store, timeout(3000).times(1))
            .setPreservationSequenceNumber(eq(PILLARID), eq(TEST_COLLECTION), anyLong());
    }
}
