package org.bitrepository.pillar.common;

import java.io.IOException;
import java.io.InputStream;

import org.bitrepository.common.filestore.FileInfo;


public class MockFileInfo implements FileInfo {

    String fileID;
    Long lastModifiedDate;
    Long size;
    InputStream is;
    
    public MockFileInfo(String fileID, Long lastModifiedDate, Long size, InputStream is) {
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
    public InputStream getInputstream() throws IOException {
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
