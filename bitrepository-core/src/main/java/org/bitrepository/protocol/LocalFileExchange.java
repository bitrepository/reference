/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.StreamUtils;

/**
 * File exchange used for exchanging files on a local filesystem 
 */
public class LocalFileExchange implements FileExchange {
    private final File storageDir;

    public LocalFileExchange(String storageDir) {
        this.storageDir = new File(storageDir);
    }

    @Override
    public void putFile(InputStream in, URL url) throws IOException {
        FileUtils.writeStreamToFile(in, new File(url.getFile()));
    }

    @Override
    public URL putFile(File dataFile) {
        try {
            URL url = getURL(dataFile.toString());
            File dest = new File(url.getFile());
            FileUtils.copyFile(dataFile, dest);
            return url;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot create the URL.", e);
        }
    }

    @Override
    public InputStream getFile(URL url) throws IOException {
        File file = new File(url.getFile());
        return new FileInputStream(file);
    }
    
    @Override
    public void getFile(OutputStream out, URL url) throws IOException {
        try(FileInputStream fis = new FileInputStream(new File(url.getFile()))) {
            StreamUtils.copyInputStreamToOutputStream(fis, out);    
        }
    }

    @Override
    public void getFile(File outputFile, String fileAddress) {
        URL url; 
        try {
            url = new URL(fileAddress);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot create the URL.", e);
        }
        
        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            getFile(out, url);
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not download data "
                    + "from '" + fileAddress + "' (url: '" + url + "') to the file '" 
                    + outputFile.getAbsolutePath() + "'.", e);
        }
    }

    @Override
    public URL getURL(String filename) throws MalformedURLException {
        File dest = new File(storageDir, new File(filename).getName());
        URL url;
        try {
            url = dest.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot create the URL.", e);
        } 
        return url;
    }

    @Override
    public void deleteFile(URL url) throws IOException, URISyntaxException {
        File fileToDelete = new File(url.getFile());
        fileToDelete.delete();
    }
}
