/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File info for the files of a default file system.
 */
public class DefaultFileInfo implements FileInfo {
    /** The file with the info.*/
    private final File file;
    
    /**
     * Constructor.
     * @param file The file for the file info.
     */
    public DefaultFileInfo(File file) {
        this.file = file;
    }
    
    @Override
    public String getFileID() {
        return file.getName();
    }
    
    @Override
    public InputStream getInputstream() throws IOException {
        return new FileInputStream(file);
    }
    
    @Override
    public Long getMdate() {
        return file.lastModified();
    }
    
    @Override
    public long getSize() {
        return file.length();
    }
    
    /**
     * @return The file.
     */
    public File getFile() {
        return file;
    }
}
