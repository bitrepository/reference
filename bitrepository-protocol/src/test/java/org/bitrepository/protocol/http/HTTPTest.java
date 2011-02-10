package org.bitrepository.protocol.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the functionality of the HTTPConnection.
 * @author jolf
 */
public class HTTPTest extends ExtendedTestCase {

    /**
     * Tests the basic functionality: put and get!
     * @throws Exception
     */
    @Test(groups = { "testfirst" })
    public void httpBasicTest() throws Exception {
        addDescription("Tests whether it is possible to upload a file to the "
                + "default http-server, and then download the file again. "
                + "Also checks whether the content of the downloaded file is "
                + "the same as the original file. Also tests if the file is "
                + "the same if it is uploaded to a different location.");
        
        // choose file and load as inputstream.
        File fil = new File("src/test/resources/test.txt");
        Assert.assertTrue(fil.isFile());
        FileInputStream is = new FileInputStream(fil);
        
        // upload file to http-server.
        addStep("Uploading file '" + fil.getName() + "'", "The file should now"
                + " be placed on the http-server.");
        URL url = HTTPConnection.put(is, fil.getName() + "-test");
        
        // read the content of the file.
        String fileContent = readFile(fil);
        
        // download the file again.
        addStep("Downloading the fil again from url '" + url.toString() + "'", 
                "Should have identical content as original file.");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HTTPConnection.get(baos, url.toString());
        
        // check whether the downloaded content is identical to the uploaded file.
        Assert.assertEquals(fileContent, baos.toString());
        
        addStep("Again uploading the file to the http-server, though on a "
                + "different location.", "Should be the same file.");
        URL url2 = HTTPConnection.put(fil);
        ByteArrayOutputStream file2 = new ByteArrayOutputStream();
        HTTPConnection.get(file2, url2.toString());
        
        Assert.assertEquals(baos.toString(), file2.toString());
    }
    
    /**
     * Method for reading the content of a file as a string.
     * @param fil The file to read as a string.
     * @return The content of the file as a string.
     * @throws IOException If there is a problem reading the file.
     */
    public String readFile(File fil) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fil));
        StringBuffer res = new StringBuffer();
        
        String line = "";
        while((line = reader.readLine()) != null) {
            res.append(line + "\n");
        }
        return res.toString();
    }
}
