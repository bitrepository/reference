package org.bitrepository.protocol.fileexchange;

import org.apache.commons.io.IOUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalFileExchangeTest {
    static final String BASE_FILE_EXCHANGE_DIR = "target/fileexchange/";
    private FileExchange exchange;

    @BeforeAll
    void setup() throws IOException {
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

    @Test
    @Tag("regressiontest")
    void getUrlTest() throws MalformedURLException, URISyntaxException {
        String testFile = "getUrlTestfile";

        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URI baseURI = new URI("file", null, basedir.getAbsolutePath() + "/", null);
        String encodedFilename = new URI(null, null, null, -1, "/" + testFile, null, null)
                .getRawPath().substring(1).replace("+", "%2B");
        URL expectedUrl = new URI(baseURI.toASCIIString() + encodedFilename).toURL();

        URL actualUrl = exchange.getURL(testFile);
        Assertions.assertEquals(expectedUrl, actualUrl);
        File actualFile = new File(actualUrl.toURI());
        Assertions.assertFalse(actualFile.exists());
    }

    /**
     * Test that filenames containing '#' character can be ingested in bitrepository
     * Filenames needs to be ingested URLEncoded meaning that the url to filenames is delivered as
     * "getUrlTestfileHashchar%23Testfragment" instead of getUrlTestfileHashchar#Testfragment
     */
    @Test
    void putFileByFileContainingHashTest() throws Exception {
        String testFileName = "getUrlTestfileHashchar#Testfragment";
        String testFileLocation = "target/" + testFileName;
        String testFileContent = "lorem ipsum1";
        File testFile = createTestFile(testFileLocation, testFileContent);

        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URI baseURI = new URI("file", null, basedir.getAbsolutePath() + "/", null);
        String encodedFilename = new URI(null, null, null, -1, "/" + testFileName, null, null)
                .getRawPath().substring(1).replace("+", "%2B");
        URL expectedUrl = new URI(baseURI.toASCIIString() + encodedFilename).toURL();

        URL fileExchangeUrl = exchange.putFile(testFile);
        Assertions.assertEquals(expectedUrl, fileExchangeUrl);
        File actualFile = new File(fileExchangeUrl.toURI());
        Assertions.assertTrue(actualFile.exists());
        String fileExchangeContent = readTestFileContent(actualFile);
        Assertions.assertEquals(testFileContent, fileExchangeContent);
        actualFile.delete();
    }

    @Test
    void putFileByFileTest() throws IOException, URISyntaxException {
        String testFileName = "putFileByFileTestFile";
        String testFileLocation = "target/" + testFileName;
        String testFileContent = "lorem ipsum1";
        File testFile = createTestFile(testFileLocation, testFileContent);

        File basedir = new File(BASE_FILE_EXCHANGE_DIR);
        URI baseURI = new URI("file", null, basedir.getAbsolutePath() + "/", null);
        String encodedFilename = new URI(null, null, null, -1, "/" + testFileName, null, null)
                .getRawPath().substring(1).replace("+", "%2B");
        URL expectedUrl = new URI(baseURI.toASCIIString() + encodedFilename).toURL();

        URL fileExchangeUrl = exchange.putFile(testFile);
        Assertions.assertEquals(expectedUrl, fileExchangeUrl);

        File actualFile = new File(fileExchangeUrl.toURI());
        Assertions.assertTrue(actualFile.exists());
        String fileExchangeContent = readTestFileContent(actualFile);
        Assertions.assertEquals(testFileContent, fileExchangeContent);
        actualFile.delete();
    }

    @Test
    void putFileByStreamTest() throws IOException, URISyntaxException {
        String testFileName = "putFileByStreamTestFile";
        String testFileContent = "lorem ipsum2";

        InputStream is = new ByteArrayInputStream(testFileContent.getBytes(StandardCharsets.UTF_8));
        URL fileExchangeUrl = exchange.getURL(testFileName);
        exchange.putFile(is, fileExchangeUrl);

        File fileExchangeFile = new File(fileExchangeUrl.toURI());
        String fileExchangeContent = readTestFileContent(fileExchangeFile);
        Assertions.assertEquals(testFileContent, fileExchangeContent);
        fileExchangeFile.delete();
    }

    @Test
    void getFileByInputStreamTest() throws IOException {
        String testFileName = "getFileByInputStreamTestFile";
        String testFileContent = "lorem ipsum3";
        String testFileLocation = "target/" + testFileName;

        File testFile = createTestFile(testFileLocation, testFileContent);
        URL testFileUrl = testFile.toURI().toURL();

        InputStream is = exchange.getFile(testFileUrl);
        String fileContent = IOUtils.toString(is, StandardCharsets.UTF_8);
        Assertions.assertEquals(testFileContent, fileContent);
    }

    @Test
    void getFileByOutputStreamTest() throws IOException {
        String testFileName = "getFileByOutputStreamTestFile";
        String testFileContent = "lorem ipsum4";
        String testFileLocation = "target/" + testFileName;

        File testFile = createTestFile(testFileLocation, testFileContent);
        URL testFileUrl = testFile.toURI().toURL();

        OutputStream os = new ByteArrayOutputStream();

        exchange.getFile(os, testFileUrl);
        Assertions.assertEquals(testFileContent, os.toString());
    }

    @Test
    void getFileByAddressTest() throws IOException {
        String testFileName = "getFileByAddressTestFile";
        String testFileContent = "lorem ipsum5";
        String testFileLocation = "target/" + testFileName;

        File testFile = createTestFile(testFileLocation, testFileContent);
        URL testFileUrl = testFile.toURI().toURL();

        File destination = new File("target/getFileByAddressTestOutputFile");
        destination.deleteOnExit();

        exchange.getFile(destination, testFileUrl.toString());
        String destinationContent = readTestFileContent(destination);
        Assertions.assertEquals(testFileContent, destinationContent);
    }

    @Test
    void deleteFileTest() throws IOException, URISyntaxException {
        String testFileName = "putFileByStreamTestFile";
        String testFileContent = "lorem ipsum6";

        InputStream is = new ByteArrayInputStream(testFileContent.getBytes(StandardCharsets.UTF_8));
        URL fileExchangeUrl = exchange.getURL(testFileName);
        exchange.putFile(is, fileExchangeUrl);

        File fileExchangeFile = new File(fileExchangeUrl.toURI());
        Assertions.assertTrue(fileExchangeFile.exists());
        exchange.deleteFile(fileExchangeUrl);
        Assertions.assertFalse(fileExchangeFile.exists());
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
