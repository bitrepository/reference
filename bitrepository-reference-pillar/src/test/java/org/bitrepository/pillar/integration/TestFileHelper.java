/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
