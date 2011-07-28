/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.putfile;

import java.net.URL;

import org.bitrepository.modify.putfile.PutClient;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.jaccept.TestEventManager;

/**
 * Wrapper class for a PutFileClient.
 */
public class PutClientTestWrapper implements PutClient {
    /** The PutClient to wrap. */
    private PutClient wrappedPutClient;
    /** The manager to monitor the operations.*/
    private TestEventManager testEventManager;

    /**
     * Constructor.
     * @param putClientInstance The instance to wrap and monitor.
     * @param eventManager The manager to monitor the operations.
     */
    public PutClientTestWrapper(PutClient putClientInstance, TestEventManager eventManager) {
        this.wrappedPutClient = putClientInstance;
        this.testEventManager = eventManager;
    }

    @Override
    public void putFileWithId(URL url, String fileId, Long fileSize, EventHandler eventHandler) 
            throws OperationFailedException {
        testEventManager.addStimuli("Calling PutFileWithId(" + url + ", " + fileId + ", " + fileSize + ", eventHandler)");
        wrappedPutClient.putFileWithId(url, fileId, fileSize, eventHandler);
    }
}
