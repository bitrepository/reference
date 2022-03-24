/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.integration.func;

import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * The parent class for pillar acceptance tests. The tests can be run in a multi pillar collection has the tests will
 * ignore responses from other pillars.
 */
public abstract class PillarFunctionTest extends PillarIntegrationTest {
    protected static final Long DEFAULT_FILE_SIZE = 10L;
    protected PutFileMessageFactory msgFactory;
    protected String testSpecificFileID;
    /** Used for receiving responses from the pillar */
    protected MessageReceiver clientReceiver;

    @BeforeMethod(alwaysRun=true)
    public void generalMethodSetup(Method method) throws Exception {
        testSpecificFileID = method.getName() + "File-" + createDate();
    }

    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();

        clientReceiver = new MessageReceiver(settingsForTestClient.getReceiverDestinationID(), testEventManager);
        addReceiver(clientReceiver);

        Collection<String> pillarFilter = Arrays.asList(testConfiguration.getPillarUnderTestID());
        clientReceiver.setFromFilter(pillarFilter);
        alarmReceiver.setFromFilter(pillarFilter);
    }
}
