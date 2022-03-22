/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.common;

import java.io.IOException;
import java.io.InputStream;

import org.bitrepository.common.filestore.FileInfo;


public class FileInfoStub implements FileInfo {

    String fileID;
    Long lastModifiedDate;
    Long size;
    InputStream is;
    
    public FileInfoStub(String fileID, Long lastModifiedDate, Long size, InputStream is) {
        this.fileID = fileID;
        this.lastModifiedDate = lastModifiedDate;
        this.size = size; 
        this.is = is;
    }
    
    @Override
    public String getFileID() {
        return fileID;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return is;
    }

    @Override
    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public long getSize() {
        if(size == null) {
            return 0L;
        }
        return size;
    }

}
