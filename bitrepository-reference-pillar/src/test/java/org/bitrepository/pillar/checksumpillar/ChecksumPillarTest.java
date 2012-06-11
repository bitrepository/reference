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
package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.service.contributor.ContributorContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ChecksumPillarTest extends DefaultFixturePillarTest {
    protected MemoryCache cache;
    protected ChecksumPillarMediator mediator;
    protected MockAlarmDispatcher alarmDispatcher;
    protected MockAuditManager audits;
    protected PillarContext context;
    protected ChecksumSpecTYPE csSpec;
    protected ChecksumDataForFileTYPE csData;
    protected static String DEFAULT_MD5_CHECKSUM = "1234cccccccc4321";

    @BeforeMethod(alwaysRun=true)
    public void initialiseChecksumPillarTest() throws Exception {
        cache = new MemoryCache();
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, componentSettings);
        alarmDispatcher = new MockAlarmDispatcher(contributorContext);
        context = new PillarContext(componentSettings, messageBus, alarmDispatcher, audits);
        mediator = new ChecksumPillarMediator(context, cache);
        mediator.start();

        csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);

        csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpec);
        csData.setChecksumValue(Base16Utils.encodeBase16(DEFAULT_MD5_CHECKSUM));
    }

    @AfterMethod(alwaysRun=true)
    public void closeArchive() {
        if(cache != null) {
            cache.cleanUp();
        }
        if(mediator != null) {
            mediator.close();
        }
    }

    @Override
    protected String getComponentID() {
        return "ChecksumPillarUnderTest";
    }
    
    protected void initializeCacheWithMD5ChecksummedFile() {
        addFixtureSetup("Initialize the Checksum pillar cache with the default file checksum.");
        
        cache.putEntry(DEFAULT_FILE_ID, DEFAULT_MD5_CHECKSUM);
    }
}
