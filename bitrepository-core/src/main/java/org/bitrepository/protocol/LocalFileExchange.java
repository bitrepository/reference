package org.bitrepository.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class LocalFileExchange implements FileExchange {
    private final File storageDir;

    public LocalFileExchange(String storageDir) {
        this.storageDir = new File(storageDir);
    }

    @Override
    public void putFile(InputStream in, URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getFile(URL url) throws IOException {
        File file = new File(storageDir, url.getFile());
        return new FileInputStream(file);
    }

    @Override
    public URL putFile(File dataFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getFile(OutputStream out, URL url) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getFile(File outputFile, String fileAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(String filename) throws MalformedURLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteFile(URL url) throws IOException, URISyntaxException {
        throw new UnsupportedOperationException();
    }
}
