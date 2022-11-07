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

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.common.utils.StreamUtils;
import org.bitrepository.settings.referencesettings.FileExchangeSettings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * File exchange used for exchanging files on a local filesystem
 */
public class LocalFileExchange implements FileExchange {
    private final FileExchangeSettings settings;

    public LocalFileExchange(FileExchangeSettings settings) {
        this.settings = settings;
    }

    @Override
    public void putFile(InputStream in, URL url) throws IOException {
        FileUtils.writeStreamToFile(in, new File(url.getFile()));
    }

    /**
     * Put the file into bitrepository and return the url of the ingested file
     *
     * @param dataFile File to get ingested
     * @return URL encoded url test#file is returned as test%23file
     */
    @Override
    public URL putFile(File dataFile) {
        try {
            File dest = new File(settings.getPath(), dataFile.getName());
            FileUtils.copyFile(dataFile, dest);
            return dest.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Cannot create the URL.", e);
        }
    }

    @Override
    public InputStream getFile(URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public void getFile(OutputStream out, URL url) throws IOException {
        try (InputStream fis = url.openStream()) {
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

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            getFile(out, url);
        } catch (IOException e) {
            throw new CoordinationLayerException("Could not download data "
                    + "from '" + fileAddress + "' (url: '" + url + "') to the file '"
                    + outputFile.getAbsolutePath() + "'.", e);
        }
    }

    @Override
    public URL getURL(String filename) throws MalformedURLException {
        ArgumentValidator.checkNotNull(settings,
                "The ReferenceSettings are missing the settings for the file exchange.");
        File dest = new File(settings.getPath(), new File(filename).getName());
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
