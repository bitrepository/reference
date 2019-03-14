package org.bitrepository.pillar.store.hadooparchive;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * HDFS archive for a single given collection.
 *
 * It will create a directory for the collection, and two sub-directories: a tempDir and a fileDir.
 * When a new file is being uploaded, it will first be put into the tempDir, and later moved to the fileDir.
 */
public class HdfsArchive {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The name for the directory with the temporary files (those about to be put into file-dir).*/
    protected static final String TEMP_PATH_NAME = "tempDir";
    /** The name of the directory with the archived files.*/
    protected static final String FILE_PATH_NAME = "fileDir";

    protected final String collectionID;
    protected final FileSystem fileSystem;
    protected final Path tempDirPath;
    protected final Path fileDirPath;

    /**
     * Constructor.
     * @param collectionID The ID of the collection.
     * @param fileSystem The FileSystem.
     * @param rootPath The root path for this archive.
     */
    protected HdfsArchive(String collectionID, FileSystem fileSystem, Path rootPath) {
        this.collectionID = collectionID;
        this.fileSystem = fileSystem;
        this.tempDirPath = new Path(rootPath, collectionID + Path.SEPARATOR + TEMP_PATH_NAME);
        this.fileDirPath = new Path(rootPath, collectionID + Path.SEPARATOR + FILE_PATH_NAME);

        try {
            if(!fileSystem.isDirectory(tempDirPath)) {
                fileSystem.mkdirs(tempDirPath);
            }
            if(!fileSystem.isDirectory(fileDirPath)) {
                fileSystem.mkdirs(fileDirPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't instantiate HDFS archive for collection '" + collectionID
                    + "'", e);
        }
    }

    /**
     * Retrieves the FileInfo for the file ID.
     * @param fileID The ID of the file.
     * @return The HDFS file info for the file ID.
     */
    public FileInfo getFileInfo(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkTrue(hasFile(fileID), "hasFile()");

        HadoopFileInfo res = new HadoopFileInfo(fileSystem, new Path(fileDirPath, fileID), fileID);
        return res;
    }

    /**
     * Checks if the given file exists.
     * @param fileID The ID of the file.
     * @return Whether it exists. Or an error occurred while looking for it.
     */
    public boolean hasFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        try {
            return fileSystem.exists(new Path(fileDirPath, fileID));
        } catch (IOException e) {
            log.warn("Issue occurred while trying to file find '" + fileID + "'", e);
            return false;
        }
    }

    /**
     * TODO: ensure, that Path.getName() delivers the file name and not more of the path.
     * @return The list of all the fileIDs for this collection.
     */
    public Collection<String> getAllFileIds() {
        try {
            List<String> res = new ArrayList<>();
            for(FileStatus fileStatus : fileSystem.listStatus(fileDirPath)) {
                res.add(fileStatus.getPath().getName());
            }
            return res;
        } catch (IOException e) {
            throw new IllegalStateException("Issue occurred while trying to retrieve all file ids for collection '"
                    + collectionID + "'", e);
        }
    }

    /**
     * Downloads the file to tempDir.
     * @param fileID The ID of the file to download.
     * @param inputStream The inputstream with the file.
     * @return The FileInfo for the file when it has been downloaded.
     * @throws IOException If it fails to download the file.
     */
    public FileInfo downloadFileForValidation(String fileID, InputStream inputStream) throws IOException {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNull(inputStream, "InputStream inputStream");

        Path tempPath = new Path(tempDirPath, fileID);
        OutputStream out = fileSystem.create(tempPath, true);

        StreamUtils.copyInputStreamToOutputStream(inputStream, out);

        return new HadoopFileInfo(fileSystem, tempPath, fileID);
    }

    /**
     * Moves a file from tempDir to fileDir.
     * @param fileID The ID of the file to move.
     */
    public void moveToArchive(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        Path tempFile = new Path(tempDirPath, fileID);
        Path archiveFile = new Path(fileDirPath, fileID);

        try {
            if(fileSystem.exists(archiveFile)) {
                throw new IllegalStateException("The file '" + fileID + "' does not exist within the tempDir.");
            }
            if(!fileSystem.exists(tempFile)) {
                throw new IllegalStateException("The file '" + fileID + "' does already exist within the fileDir.");
            }

            // TODO: is it local from local seems wrong?
            fileSystem.moveFromLocalFile(tempFile, archiveFile);
        } catch (IOException e) {
            throw new IllegalStateException("Error occurred while trying to move the file '" + fileID
                    + "' to archive.", e);
        }
    }

    /**
     * Deletes a file.
     * @param fileID The ID of the file to delete.
     */
    public void deleteFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        if(!hasFile(fileID)) {
            log.info("Trying to delete file, which does not exist.");
            return;
        }
        try {
            fileSystem.delete(new Path(fileDirPath, fileID), false);
        } catch (IOException e) {
            throw new IllegalStateException("Failure when trying to delete '" + fileID + "'.", e);
        }
    }

    /**
     * Replaces a file. First removes it from the fileDir, then moves the one from tempDir.
     * @param fileID The ID of the file to replace.
     */
    public void replaceFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        deleteFile(fileID);
        moveToArchive(fileID);
    }

    /**
     * @return The bytes left in the archive.
     */
    public long sizeLeftInArchive() {
        try {
            return fileSystem.getStatus().getRemaining();
        } catch (IOException e) {
            log.warn("Failed to retrieve the remaining space left in HDFS archive.", e);
            return -1;
        }
    }

    /**
     * Retrieves the file info for the file in the tempDir.
     * @param fileID The ID of the file.
     * @return The file info for the file in tempDir.
     */
    public FileInfo getFileInTmpDir(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        return new HadoopFileInfo(fileSystem, new Path(tempDirPath, fileID), fileID);
    }

    /**
     * Removes the file from tempDir, if it exist therein.
     * @param fileID The ID of the file, which must not exist in the tempDir.
     */
    public void ensureFileNotInTmpDir(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        Path tempFilePath = new Path(fileDirPath, fileID);
        try {
            if(fileSystem.exists(tempFilePath)) {
                fileSystem.delete(tempFilePath, false);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot ensure that the file '" + fileID + "' from tempDir at collection '"
                    + collectionID + "'.", e);
        }
    }
}
