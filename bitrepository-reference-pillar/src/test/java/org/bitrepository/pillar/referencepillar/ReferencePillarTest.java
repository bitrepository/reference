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
package org.bitrepository.pillar.referencepillar;

import java.io.ByteArrayInputStream;
import java.io.File;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.cache.MemoryCache;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.service.audit.MockAuditManager;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ReferencePillarTest extends DefaultFixturePillarTest {
    protected ReferenceArchive archive;
    protected ReferenceChecksumManager csManager;
    protected ReferencePillarMediator mediator;
    protected MockAuditManager audits;
    protected ChecksumStore csCache;
    protected MessageHandlerContext context;
    
    protected final String EMPTY_FILE_CHECKSUM = "d41d8cd98f00b204e9800998ecf8427e";

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        File fileDir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0));
        componentSettings.getReferenceSettings().getPillarSettings().setAlarmLevel(AlarmLevel.WARNING);
        if(fileDir.exists()) {
            FileUtils.delete(fileDir);
        }

        addStep("Initialize the pillar.", "Should not be a problem.");
        csCache = new MemoryCache();
        archive = new ReferenceArchive(componentSettings.getReferenceSettings().getPillarSettings().getFileDir());
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, componentSettings);
        context = new MessageHandlerContext(
                componentSettings,
                messageBus,
                new PillarAlarmDispatcher(contributorContext), audits);
        csManager = new ReferenceChecksumManager(archive, csCache, ChecksumUtils.getDefault(context.getSettings()), 
                3600000L);
        mediator = new ReferencePillarMediator(messageBus, context, archive, csManager);
        mediator.start();
    }


    @AfterMethod(alwaysRun=true)
    public void closeArchive() {
        File dir = new File(componentSettings.getReferenceSettings().getPillarSettings().getFileDir().get(0));
        if(dir.exists()) {
            FileUtils.delete(dir);
        }

        if(mediator != null) {
            mediator.close();
        }
    }

    @Override
    protected String getComponentID() {
        return "ReferencePillarUnderTest";
    }
    
    protected void initializeArchiveWithEmptyFile() {
        addFixtureSetup("Initialize the Reference pillar cache with en empty file.");
        try {
            archive.downloadFileForValidation(DEFAULT_FILE_ID, new ByteArrayInputStream(new byte[0]));
            archive.moveToArchive(DEFAULT_FILE_ID);
        } catch (Exception e) {
            Assert.fail("Could not instantiate the archive", e);
        }
    }
}
