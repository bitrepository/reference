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
package org.bitrepository.pillar.messagehandling;

import static org.mockito.Mockito.mock;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.common.SettingsHelper;
import org.bitrepository.pillar.messagehandler.PillarMediator;
import org.bitrepository.pillar.store.PillarModel;
import org.bitrepository.service.audit.MockAuditManager;
import org.bitrepository.service.contributor.ResponseDispatcher;

public abstract class MockedPillarTest extends DefaultFixturePillarTest {
    protected PillarMediator mediator;
    protected PillarModel model;
    protected MockAuditManager audits;
    protected MessageHandlerContext context;
    protected ChecksumSpecTYPE csSpec;
    protected ChecksumDataForFileTYPE csData;
    protected ChecksumDataForFileTYPE NON_DEFAULT_CS;
    protected static String DEFAULT_MD5_CHECKSUM = "1234cccccccc4321";
    protected static String NON_DEFAULT_MD5_CHECKSUM = "1234cccccccc4322";

    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        audits = new MockAuditManager();
        model = mock(PillarModel.class);

        createPillar();
    }

    @Override
    protected void shutdownCUT() {
        shutdownMediator();
        super.shutdownCUT();
    }

    /**
     * Used for creating a 'clean' checksum pillar based on the current configuration.
     */
    protected void createPillar() {
        shutdownMediator();
        addFixture("Initialize a new checksumPillar.");
        PillarAlarmDispatcher alarmDispatcher = new PillarAlarmDispatcher(settingsForCUT, messageBus);
        context = new MessageHandlerContext(settingsForCUT,
                SettingsHelper.getPillarCollections(settingsForCUT.getComponentID(), settingsForCUT.getCollections()),
                new ResponseDispatcher(settingsForCUT, messageBus), 
                alarmDispatcher,
                audits);
        mediator = new PillarMediator(messageBus, context, model);
        mediator.start();

        csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);

        csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpec);
        csData.setChecksumValue(Base16Utils.encodeBase16(DEFAULT_MD5_CHECKSUM));

        NON_DEFAULT_CS = new ChecksumDataForFileTYPE();
        NON_DEFAULT_CS.setCalculationTimestamp(CalendarUtils.getEpoch());
        NON_DEFAULT_CS.setChecksumSpec(csSpec);
        NON_DEFAULT_CS.setChecksumValue(Base16Utils.encodeBase16(NON_DEFAULT_MD5_CHECKSUM));
    }

    public void shutdownMediator() {
        if(mediator != null) {
            mediator.close();
            mediator = null;
        }
    }

    @Override
    protected String getComponentID() {
        return "ChecksumPillar-" + testMethodName;
    }
}
