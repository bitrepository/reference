package org.bitrepository.pillar.integration;

import java.io.File;
import java.util.Date;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

public class TestFileHelper {
    public static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;

    private final Settings settings;
    private final HttpServerConnector httpConnector;

    public TestFileHelper(
            Settings settings, HttpServerConnector httpConnector) {
        this.settings = settings;
        this.httpConnector = httpConnector;
    }

    public static File getFile() {
        return getFile(DEFAULT_FILE_ID);
    }

    public static String getFileName(File file) {
        return DEFAULT_FILE_ID + new Date().getTime();
    }

    public static long getFileSize(File file) {
        return file.length();
    }

    public static File getFile(String name) {
        File file = new File("src/test/resources/" + name);
        assert(file.isFile());
        return file;
    }

    public void putFileOnWebdavServer() {
    }
}
