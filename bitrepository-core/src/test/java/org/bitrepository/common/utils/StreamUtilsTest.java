package org.bitrepository.common.utils;

import java.io.ByteArrayOutputStream;

import org.apache.activemq.util.ByteArrayInputStream;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StreamUtilsTest extends ExtendedTestCase {
    String DATA = "The data for the streams.";
    @Test(groups = {"regressiontest"})
    public void streamTester() throws Exception {
        addDescription("Tests the SteamUtils class.");
        addStep("Setup variables", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(DATA.getBytes());

        addStep("Test with null arguments", "Should throw exceptions");
        try {
            StreamUtils.copyInputStreamToOutputStream(null, out);
            Assert.fail("Should throw an exception here.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        
        try {
            StreamUtils.copyInputStreamToOutputStream(in, null);
            Assert.fail("Should throw an exception here.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
        
        addStep("Test copying the input stream to the output stream.", "Should contain the same data.");
        StreamUtils.copyInputStreamToOutputStream(in, out);
        
        Assert.assertEquals(new String(out.toByteArray()), DATA);
    }

}
