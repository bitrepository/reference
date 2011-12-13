package org.bitrepository.common;

import org.bitrepository.common.utils.ChecksumUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumTest extends ExtendedTestCase {
    
    @Test(groups = { "regressiontest" })
    public void calculateChecksums() {
        addDescription("Tests whether the utility class for calculating checksums is able to correctly calculate "
                + "predefined examples from :"
                + "http://en.wikipedia.org/wiki/HMAC#Examples_of_HMAC_.28MD5.2C_SHA1.2C_SHA256_.29");
        addStep("Test with no text and no key for MD5, SHA1, and SHA256", 
                "Should give expected results.");
        Assert.assertEquals(ChecksumUtils.generateChecksum("", "md5", ""), 
                "74e6f7298a9c2d168935f58c001bad88");
        Assert.assertEquals(ChecksumUtils.generateChecksum("", "sha1", ""), 
                "fbdb1d1b18aa6c08324b7d64b71fb76370690e1d");
        Assert.assertEquals(ChecksumUtils.generateChecksum("", "sha256", ""), 
                "b613679a0814d9ec772f95d778c35fc5ff1697c493715653c6c712144292c5ad");

        String message = "The quick brown fox jumps over the lazy dog";
        String key = "key";
        addStep("Test with the text '" + message + "' and key '" + key + "' for MD5, SHA1, and SHA256", 
                "Should give expected results.");
        Assert.assertEquals(ChecksumUtils.generateChecksum(message, "md5", key),
                "80070713463e7749b90c2dc24911e275");
        Assert.assertEquals(ChecksumUtils.generateChecksum(message, "sha1", key),
                "de7c9b85b8b78aa6bc8a7a36f70a90701c9db4d9");
        Assert.assertEquals(ChecksumUtils.generateChecksum(message, "sha256", key),
                "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
    }
}
