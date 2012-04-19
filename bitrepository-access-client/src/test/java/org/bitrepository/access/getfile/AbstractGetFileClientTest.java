/*
 * #%L
 * Bitrepository Access
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
/*
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

import java.io.File;

import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractGetFileClientTest extends DefaultFixtureClientTest   {
    protected TestGetFileMessageFactory testMessageFactory;
    protected TestFileStore pillar1FileStore;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        if (useMockupPillar()) {
            testMessageFactory = new TestGetFileMessageFactory(
                    settings.getCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1", new File("src/test/resources/test-files/", DEFAULT_FILE_ID));
            // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
            // with content.
        }
        httpServer.clearFiles();
    }
}
