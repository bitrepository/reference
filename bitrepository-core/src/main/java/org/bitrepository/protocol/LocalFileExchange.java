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
