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
