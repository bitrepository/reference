/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id: HTTPTest.java 124 2011-03-25 09:04:11Z kfc $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-protocol/src/test/java/org/bitrepository/protocol/http/HTTPTest.java $
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
package org.bitrepository.protocol.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests the functionality of the HTTPConnection.
 */
public class HTTPSTest extends ExtendedTestCase {
    
    /** The path to the 'test.txt' test for the httpBasicTest.*/
    public static final String PATH_TO_TEST_TXT = "src/test/resources/test.txt";
    /** The path to the certificate.*/
    public static final String PATH_TO_CERTIFICATE_FILE = "src/test/resources/cert";
    
    /** The directory for temporary data, which will be create during the test and removed afterwards.*/
    public static final File TEMP_DIR = new File("temp");
    /** The keystore file. Will be created during the test, and removed afterwards.*/
    public static final File KEYSTORE_FILE = new File(TEMP_DIR, "keystore");
    
    /**
     * Called for initialising the tests.
     */
    @BeforeTest (alwaysRun = true)
    public void setUp() {
    	// Ensure, that the temporary directory is empty.
    	if(TEMP_DIR.exists()) {
    		FileUtils.delete(TEMP_DIR);
    	}
    	TEMP_DIR.mkdir();
    }
    
    /**
     * Called for cleaning up after the test.
     */
    @AfterTest (alwaysRun = true)
    public void tearDown() {
    	// Remove the temporary directory.
    	if(TEMP_DIR.exists()) {
    		System.out.println("Removing the TEMP_DIR '" + TEMP_DIR + "'");
    		FileUtils.delete(TEMP_DIR);
    	}
    }
    
    

    /**
     * Tests the basic functionality: put and get!
     * @throws Exception
     */
    @Test(groups = { "regressiontest" })
    public final void httpsBasicTest() throws Exception {
    	addDescription("Tests whether it is possible to upload a file to the "
    			+ "default http-server, and then download the file again. "
    			+ "Also checks whether the content of the downloaded file is "
    			+ "the same as the original file. Also tests if the file is "
    			+ "the same if it is uploaded to a different location.");
    	addStep("Retrieve the file to upload with the HTTPS tool and make it into a input stream", 
    	"The file should exist and the operation should be legal.");
    	// choose file and load as inputstream.
    	File fil = new File(PATH_TO_TEST_TXT);
    	FileInputStream fis = new FileInputStream(fil);

    	addStep("Read the content of the file, for later verification", "Should be possible.");
    	// read the content of the file.
    	String fileContent = readFile(fil);

    	addStep("Inserting the variables for this test into the configuration.", "Should be allowed.");
    	ProtocolComponentFactory.getInstance().getProtocolConfiguration().getFileExchangeConfigurations().setFileExchangeClass(HTTPSFileExchange.class.getName());
    	ProtocolComponentFactory.getInstance().getProtocolConfiguration().getFileExchangeConfigurations().setHttpsCertificateAlias("sandkasse");
    	ProtocolComponentFactory.getInstance().getProtocolConfiguration().getFileExchangeConfigurations().setHttpsCertificatePath(PATH_TO_CERTIFICATE_FILE);
    	ProtocolComponentFactory.getInstance().getProtocolConfiguration().getFileExchangeConfigurations().setHttpsKeystorePath(KEYSTORE_FILE.getPath());
    	ProtocolComponentFactory.getInstance().getProtocolConfiguration().getFileExchangeConfigurations().setHttpsKeyStorePassword("1234");

    	addStep("Initialising FileExchange connectino", "Is should be a instance of HTTPSFileExchange.");
    	FileExchange fileExchange = ProtocolComponentFactory.getInstance().getFileExchange();
    	Assert.assertTrue(fileExchange instanceof HTTPSFileExchange, "Should be instance of HTTPSFileExchange");
    	Assert.assertTrue(KEYSTORE_FILE.exists(), "The keystore file should have been created during initalisation of"
    			+ " the fileexchange.");

    	addStep("Uploading the file '" + fil.getName() + "' through the HTTPS connection", 
    	"Should be allowed.");
    	URL url = ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(fis, 
    			"HTTPS_" + fil.getName());

    	// TODO
    	addStep("TODO: make manual download.", "Not implemented yet, thus no errors.");

    	// download the file again.
    	addStep("Downloading the fil again from url '" + url.toString() + "'", 
    	"Should have identical content as original file.");
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ProtocolComponentFactory.getInstance().getFileExchange().downloadFromServer(baos, url);
    	// check whether the downloaded content is identical to the uploaded file.
    	Assert.assertEquals(fileContent, baos.toString());

    	addStep("Again uploading the file to the HTTPS-server, though on a "
    			+ "different location.", "Should be the same file.");
    	URL url2 = ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(fil);
    	
    	addStep("Download and validate the newly uploaded file.", "Should be the same as before.");
    	ByteArrayOutputStream file2 = new ByteArrayOutputStream();
    	ProtocolComponentFactory.getInstance().getFileExchange().downloadFromServer(file2, url2);
    	Assert.assertEquals(baos.toString(), file2.toString());
    }

    /**
     * Method for reading the content of a file as a string.
     * @param fil The file to read as a string.
     * @return The content of the file as a string.
     * @throws IOException If there is a problem reading the file.
     */
    public final String readFile(File fil) throws IOException {
    	BufferedReader reader = new BufferedReader(new FileReader(fil));
    	StringBuffer res = new StringBuffer();

    	String line = "";
    	while((line = reader.readLine()) != null) {
    		res.append(line + "\n");
    	}
    	return res.toString();
    }
}
