package org.bitrepository.protocol.utils;

import java.io.File;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigLoaderTest extends ExtendedTestCase {
    
    @Test(groups = { "regressiontest" })
    public void testLoadingConfig() throws Exception {
        addDescription("Test the loading of a configuration file for the config loader.");
        addStep("Setup variables", "");
        String badFilePath = "iDoNotExist.xml";
        Assert.assertFalse(new File(badFilePath).exists());
        String goodFilePath = "src/test/resources/logback-test.xml";
        Assert.assertTrue(new File(goodFilePath).exists());
        
        addStep("Test with a invalid file path", "Should throw an exception");
        try {
            new LogbackConfigLoader(badFilePath);
            Assert.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test when the file is unreadable", "Should throw an exception");
        File goodFile = new File(goodFilePath);
        try {
            goodFile.setReadable(false);
            
            try {
                new LogbackConfigLoader(goodFilePath);
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
        new LogbackConfigLoader(goodFilePath);        
    }
    
}
