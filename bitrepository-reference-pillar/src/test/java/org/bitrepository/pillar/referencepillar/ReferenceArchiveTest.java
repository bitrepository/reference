/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
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
package org.bitrepository.pillar.referencepillar;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.service.audit.MockAuditManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ReferenceArchiveTest extends ReferencePillarTest {
    protected ReferenceArchive archive;
    protected ReferencePillarMediator mediator;
    protected MockAlarmDispatcher alarmDispatcher;
    protected MockAuditManager audits;
    protected MessageHandlerContext context;
    
    private static String DIR_NAME = "archive-directory";
    private static String FILE_DIR_NAME = DIR_NAME + "/fileDir";
    
    private static String EXISTING_FILE = "file1";
    private static String MISSING_FILE = "Missing-filE";
    
    @AfterMethod (alwaysRun=true)
    public void shutdownTests() throws Exception {
        FileUtils.delete(new File(DIR_NAME));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testReferenceArchive() throws Exception {
        addDescription("Test the ReferenceArchive.");
        addStep("Setup", "Should be OK.");
        
        ReferenceArchive archive = new ReferenceArchive(Arrays.asList(DIR_NAME));
        createExistingFile();
        
        addStep("test 'hasFile'", "Should be true for the existing one and false for the missing one.");
        Assert.assertTrue(archive.hasFile(EXISTING_FILE));
        Assert.assertFalse(archive.hasFile(MISSING_FILE));
        
        addStep("Test 'getFile'", "Should be ok for the existing file and throw an exception on the missing");
        archive.getFile(EXISTING_FILE);
        try {
            archive.getFile(MISSING_FILE);
            Assert.fail("Should throw an exception when getting a missing file.");
        } catch (Exception e) {
            // expected
        }
        
        addStep("Test getAllFileIDs", "Should only deliver the existing file");
        Assert.assertEquals(archive.getAllFileIds(), Arrays.asList(EXISTING_FILE));
        
        addStep("Test 'getFileAsInputstream'", "Should only be able to deliver the existing file.");
        archive.getFileAsInputstream(EXISTING_FILE);
        try {
            archive.getFileAsInputstream(MISSING_FILE);
            Assert.fail("Should throw an exception when getting a missing file.");
        } catch (Exception e) {
            // expected
        }
        
        addStep("Delete, recreate and delete again", "Should be moved to retain dir twice.");
        archive.deleteFile(EXISTING_FILE);
        createExistingFile();
        archive.deleteFile(EXISTING_FILE);
        createExistingFile();
        Assert.assertTrue(new File(DIR_NAME + "/retainDir/" + EXISTING_FILE + ".old").isFile());
        
        addStep("Try to delete missing file.", "Should throw an exception");
        try {
            archive.deleteFile(MISSING_FILE);
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // Expected.
        }
        
        addStep("Replace a file, which does not exist in the filedir.", "Should throw an exception");
        try {
            archive.replaceFile(MISSING_FILE);
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // Expected.
        }
        
        addStep("Copy a file into the tmpDir and then use replace.", "Should create another file in retain dir and remove the one in tmpDir.");
        FileUtils.copyFile(new File(DIR_NAME + "/retainDir/" + EXISTING_FILE), 
                new File(DIR_NAME + "/tmpDir/" + EXISTING_FILE));
        archive.replaceFile(EXISTING_FILE);
        Assert.assertFalse(new File(DIR_NAME + "/tmpDir/" + EXISTING_FILE).isFile());
        Assert.assertTrue(new File(DIR_NAME + "/retainDir/" + EXISTING_FILE + ".old.old").isFile());
        
        addStep("Try performing the replace, when the file in the tempdir has been removed.", "Should throw an exception");
        try {
            archive.replaceFile(EXISTING_FILE);
            Assert.fail("Should throw an exception here.");
        } catch (IllegalStateException e) {
            // Expected.
        }
        
        archive.close();
    }
    
    private void createExistingFile() throws Exception {
        FileWriter fw = new FileWriter(new File(FILE_DIR_NAME, EXISTING_FILE), false);
        fw.write("test-data\n");
        fw.flush();
        fw.close();
    }
}