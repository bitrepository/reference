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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowContext;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
public class GetFileStepTest extends WorkflowstepTest {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";

    @Test(groups = {"regressiontest"})
    public void testPositiveReply() throws Exception {
        addDescription("Test the step for getting the file can handle COMPLETE operation event.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[3];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFile(
                eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(EventHandler.class), anyString());

        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        URL uploadUrl = new URL("http://localhost/dav/test.txt");
        GetFileStep step = new GetFileStep(context, TEST_COLLECTION, TEST_FILE_1, uploadUrl);

        step.performStep();
        verify(collector).getFile(eq(TEST_COLLECTION), eq(TEST_FILE_1), eq(uploadUrl),
                any(EventHandler.class), anyString());
        verifyNoMoreInteractions(collector);
        verifyNoMoreInteractions(alerter);
        verifyNoMoreInteractions(model);
    }
    
    @Test(groups = {"regressiontest"}, expectedExceptions = IllegalStateException.class)
    public void testNegativeReply() throws Exception {
        addDescription("Test the step for getting the file can handle FAILURE operation event.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[3];
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
                return null;
            }
        }).when(collector).getFile(
                eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(EventHandler.class), anyString());

        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        URL uploadUrl = new URL("http://localhost/dav/test.txt");
        GetFileStep step = new GetFileStep(context, TEST_COLLECTION, TEST_FILE_1, uploadUrl);

        step.performStep();
    }

}
