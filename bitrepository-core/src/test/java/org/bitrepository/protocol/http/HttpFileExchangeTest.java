package org.bitrepository.protocol.http;

import java.io.File;
import java.net.URL;

import org.bitrepository.protocol.IntegrationTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpFileExchangeTest extends IntegrationTest {
    
    @Test(groups = { "infrastructure" })
    public void uploadTest() throws Exception {
        addDescription("Test uploading a file.");
        
        HttpFileExchange hfe = new HttpFileExchange(settingsForTestClient);
        File f = new File("src/test/resources/test-files/default-test-file.txt");
        URL url = hfe.uploadToServer(f);
        
        Assert.assertNotNull(url, "URL url");
    }
}
