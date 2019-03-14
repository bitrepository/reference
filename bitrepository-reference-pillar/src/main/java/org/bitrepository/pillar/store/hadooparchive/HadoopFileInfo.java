package org.bitrepository.pillar.store.hadooparchive;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.bitrepository.common.filestore.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * File Info for the HDFS file system.
 */
public class HadoopFileInfo implements FileInfo {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The path for the file.*/
    protected final Path filePath;
    /** The file system containing the file.*/
    protected final FileSystem fileSystem;
    /** The ID of the file.*/
    protected final String fileID;

    /**
     * Constructor.
     * @param fileSystem The FileSystem containing the file.
     * @param path The path to the file.
     * @param fileID The ID of the file.
     */
    public HadoopFileInfo(FileSystem fileSystem, Path path, String fileID) {
        this.fileSystem = fileSystem;
        this.filePath = path;
        this.fileID = fileID;
    }

    @Override
    public String getFileID() {
        return fileID;
    }

    @Override
    public InputStream getInputstream() throws IOException {
        return fileSystem.open(filePath);
    }

    @Override
    public Long getLastModifiedDate() {
        try {
            return fileSystem.getFileStatus(filePath).getModificationTime();
        } catch (IOException e) {
            log.warn("Cannot get file modification date for '" + fileID + "'. Returning -1.", e);
            return -1l;
        }
    }

    @Override
    public long getSize() {
        try {
            return fileSystem.getFileStatus(filePath).getLen();
        } catch (IOException e) {
            log.warn("Cannot get file size for '" + fileID + "'. Returning -1.", e);
            return -1;
        }
    }
}
