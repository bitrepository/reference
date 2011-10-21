/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getfile;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.testng.Assert;

public class GetFileClientBlockingTest extends AbstractGetFileClientTest  {

    //@Test(groups = {"specification-only"})
    public void blocking() {
        addDescription("Tests that the blocking usage of the GetClient works correctly in the trivial case");
        addStep("Request a file from a specific using the blocking get method", 
                "The method should return after the complete message has been received");
    }
    
    //S@Test(groups = {"specification-only"})
    public void blockingWithError() throws Exception {
        addDescription("Tests that the blocking usage of the GetClient works correctly in case of an error occuring");
        addStep("Request a file from a specific non-existing pillar using the blocking get method", 
                "The method should block until a identify timeout occures, causing a TimeoutException to be thrown");
        
        settings.getCollectionSettings().getClientSettings().setIdentificationTimeout(defaultTime);
        GetFileClient getFileClient = 
            new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(settings), 
                    testEventManager);
        try {
            getFileClient.getFileFromSpecificPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), "Invalid pillar");
            Assert.fail("Expected NoPillarFoundException after timeout, but nothing happened");
        } catch (NoPillarFoundException e) {
            //As expected
        } 
    }
}
