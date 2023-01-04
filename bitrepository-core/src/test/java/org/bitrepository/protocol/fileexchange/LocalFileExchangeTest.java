package org.bitrepository.protocol.fileexchange;

import org.apache.commons.io.IOUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import org.fusesource.hawtbuf.ByteArrayInputStream;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalFileExchangeTest extends ExtendedTestCase  {
    final static String BASE_FILE_EXCHANGE_DIR = "target/fileexchange/";
    private FileExchange exchange;

    @BeforeClass(alwaysRun = true)
    public void setup() throws IOException {
        createFileExchangeDir();
        FileExchangeSettings settings = new FileExchangeSettings();
        settings.setPath(BASE_FILE_EXCHANGE_DIR);
        exchange = new LocalFileExchange(settings);
    }

    private void createFileExchangeDir() throws IOException {
        try {
            Files.createDirectory(Paths.get(BASE_FILE_EXCHANGE_DIR));
        } catch (FileAlreadyExistsException e) {
            // fine it was there before..
        }
    }

    @Test(groups = {"regressiontest"})
    public void getUrlTest() throws MalformedURLException {
        String testFile = "getUrlTestfile";
        
        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URL expectedUrl = new URL("file:" + basedir.getAbsolutePath() + "/" + testFile);
        
        URL actualUrl = exchange.getURL(testFile);
        Assert.assertEquals(actualUrl, expectedUrl);
        File actualFile = new File(actualUrl.getFile());
        Assert.assertFalse(actualFile.exists());
    }

    /**
     * Test that filenames containing '#' character can be ingested in bitrepository
     * Filenames needs to be ingested URLEncoded meaning that the url to filenames is delivered as
     * "getUrlTestfileHashchar%23Testfragment" instead of getUrlTestfileHashchar#Testfragment
     */
    @Test
    public void putFileByFileContainingHashTest() throws Exception {
        String testFileName = "getUrlTestfileHashchar#Testfragment";
        String testFileLocation = "target/" + testFileName;
        String testFileContent = "lorem ipsum1";
        File testFile = createTestFile(testFileLocation, testFileContent);

        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URL expectedUrl = new URL("file:" + basedir.getAbsolutePath() + "/" + URLEncoder.encode(testFileName,
                StandardCharsets.UTF_8));

        URL fileExchangeUrl = exchange.putFile(testFile);
        Assert.assertEquals(fileExchangeUrl, expectedUrl);
        File actualFile = new File(fileExchangeUrl.toURI());
        Assert.assertTrue(actualFile.exists());
        String fileExchangeContent = readTestFileContent(actualFile);
        Assert.assertEquals(fileExchangeContent, testFileContent);
        actualFile.delete();
    }
    
    @Test
    public void putFileByFileTest() throws IOException {
        String testFileName = "putFileByFileTestFile";
        String testFileLocation = "target/" + testFileName;
        String testFileContent = "lorem ipsum1";
        File testFile = createTestFile(testFileLocation, testFileContent);
        
        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URL expectedUrl = new URL("file:" + basedir.getAbsolutePath() + "/" + testFileName);
        
        URL fileExchangeUrl = exchange.putFile(testFile);
        Assert.assertEquals(fileExchangeUrl, expectedUrl);
        
        File actualFile = new File(fileExchangeUrl.getFile());
        Assert.assertTrue(actualFile.exists());
        String fileExchangeContent = readTestFileContent(actualFile);
        Assert.assertEquals(fileExchangeContent, testFileContent);
        actualFile.delete();
    }
    
    @Test
    public void putFileByStreamTest() throws IOException {
        String testFileName = "putFileByStreamTestFile";
        String testFileContent = "lorem ipsum2";
        
        InputStream is = new ByteArrayInputStream(testFileContent.getBytes(StandardCharsets.UTF_8));
        URL fileExchangeUrl = exchange.getURL(testFileName);
        exchange.putFile(is, fileExchangeUrl);
        
        File fileExchangeFile = new File(fileExchangeUrl.getFile());
        String fileExchangeContent = readTestFileContent(fileExchangeFile);
        Assert.assertEquals(fileExchangeContent, testFileContent);
        fileExchangeFile.delete();
    }
    
    @Test
    public void getFileByInputStreamTest() throws IOException {
        String testFileName = "getFileByInputStreamTestFile";
        String testFileContent = "lorem ipsum3";
        String testFileLocation = "target/" + testFileName;
        
        File testFile = createTestFile(testFileLocation, testFileContent);
        URL testFileUrl = testFile.toURI().toURL();
        
        InputStream is = exchange.getFile(testFileUrl);
        String fileContent = IOUtils.toString(is, StandardCharsets.UTF_8);
        Assert.assertEquals(fileContent, testFileContent);
    }
    
    @Test
    public void getFileByOutputStreamTest() throws IOException {
        String testFileName = "getFileByOutputStreamTestFile";
        String testFileContent = "lorem ipsum4";
        String testFileLocation = "target/" + testFileName;
        
        File testFile = createTestFile(testFileLocation, testFileContent);
        URL testFileUrl = testFile.toURI().toURL();
        
        OutputStream os = new ByteArrayOutputStream();
        
        exchange.getFile(os, testFileUrl);
        Assert.assertEquals(os.toString(), testFileContent);
    }
    
    @Test
    public void getFileByAddressTest() throws IOException {
        String testFileName = "getFileByAddressTestFile";
        String testFileContent = "lorem ipsum5";
        String testFileLocation = "target/" + testFileName;

        File testFile = createTestFile(testFileLocation, testFileContent);
        URL testFileUrl = testFile.toURI().toURL();
        
        File destination = new File("target/getFileByAddressTestOutputFile");
        destination.deleteOnExit();
        
        exchange.getFile(destination, testFileUrl.toString());
        String destinationContent = readTestFileContent(destination);
        Assert.assertEquals(destinationContent, testFileContent);
    }

    @Test
    public void deleteFileTest() throws IOException, URISyntaxException {
        String testFileName = "putFileByStreamTestFile";
        String testFileContent = "lorem ipsum6";
        
        InputStream is = new ByteArrayInputStream(testFileContent.getBytes(StandardCharsets.UTF_8));
        URL fileExchangeUrl = exchange.getURL(testFileName);
        exchange.putFile(is, fileExchangeUrl);
        
        File fileExchangeFile = new File(fileExchangeUrl.getFile());
        Assert.assertTrue(fileExchangeFile.exists());
        exchange.deleteFile(fileExchangeUrl);
        Assert.assertFalse(fileExchangeFile.exists());
    }
    
    private File createTestFile(String filename, String content) throws IOException {
        Files.write(Paths.get(filename), content.getBytes(StandardCharsets.UTF_8));
        File f = Paths.get(filename).toFile();
        f.deleteOnExit();
        return f;
    }
    
    private String readTestFileContent(File testFile) throws IOException {
        return Files.readString(Paths.get(testFile.toURI()));
    }
    
}
