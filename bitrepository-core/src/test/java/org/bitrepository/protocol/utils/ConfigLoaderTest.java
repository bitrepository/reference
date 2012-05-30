package org.bitrepository.protocol.utils;

import java.io.File;

import org.bitrepository.common.utils.FileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigLoaderTest extends ExtendedTestCase {
    
    String GOOD_FILE_PATH = "logback-test.xml";
    
    @BeforeMethod (alwaysRun = true)
    public void setup() {
        FileUtils.copyFile(new File("src/test/resources/logback-test.xml"), new File(GOOD_FILE_PATH));
    }
    
    @AfterMethod (alwaysRun = true)
    public void teardown() {
        FileUtils.delete(new File(GOOD_FILE_PATH));
    }
        
    @Test(groups = { "regressiontest" })
    public void testLoadingConfig() throws Exception {
        addDescription("Test the loading of a configuration file for the config loader.");
        addStep("Setup variables", "");
        String badFilePath = "iDoNotExist.xml";
        Assert.assertFalse(new File(badFilePath).exists());
        Assert.assertTrue(new File(GOOD_FILE_PATH).exists());
        
        addStep("Test with a invalid file path", "Should throw an exception");
        try {
            new LogbackConfigLoader(badFilePath);
            Assert.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test when the file is unreadable", "Should throw an exception");
        File goodFile = new File(GOOD_FILE_PATH);
        try {
            goodFile.setReadable(false);
            
            try {
                new LogbackConfigLoader(GOOD_FILE_PATH);
                Assert.fail("Should throw an exception");
            } catch (IllegalArgumentException e) {
                // expected
            }
        } finally {
            goodFile.setReadable(true);
            goodFile.setExecutable(true);
            goodFile.setWritable(true);
        }
        
        addStep("success case", "Should not throw an exception");
        new LogbackConfigLoader(GOOD_FILE_PATH);        
    }
    
}
