package org.bitrepository.pillar.referencepillar.archive;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.FileUtils;

/**
 * Manager interface for a given archival directory, with the subdirectories 'tempDir', 'fileDir' and 'retainDir'.
 * A new file is ingested into the 'tempDir', where it can be validated before it is moved to the 'fileDir'.
 * If a file is to be deleted, then it is moved from the 'fileDir' to the 'retainDir'.
 */
public class ArchiveDirectory {
    /** Constant for the temporary directory name.*/
    public static final String TEMPORARY_DIR = "tmpDir";
    /** Constant for the file archive name.*/
    public static final String ARCHIVE_DIR = "fileDir";
    /** Constant for the retain directory name.*/
    public static final String RETAIN_DIR = "retainDir";

    /** The directory for the files. Contains three sub directories: tempDir, fileDir and retainDir.*/
    private File baseDepositDir;

    /** The directory where files are being downloaded to before they are put into the filedir. */
    private File tmpDir;
    /** The directory where the files are being stored.*/
    private final File fileDir;
    /** The directory where the files are moved, when they are removed from the archive.*/
    private final File retainDir;

    /** 
     * Constructor. Initialises the file directory. 
     * 
     * @param dir The directory for this archive.
     */
    public ArchiveDirectory(String dirName) {
        ArgumentValidator.checkNotNullOrEmpty(dirName, "String dirName");

        // Instantiate the directories for this archive.
        baseDepositDir = FileUtils.retrieveDirectory(dirName);
        tmpDir = FileUtils.retrieveSubDirectory(baseDepositDir, TEMPORARY_DIR);
        fileDir = FileUtils.retrieveSubDirectory(baseDepositDir, ARCHIVE_DIR);
        retainDir = FileUtils.retrieveSubDirectory(baseDepositDir, RETAIN_DIR);
    }
    
    /**
     * @param fileId The id of the file to retrieve.
     * @return The requested file, or a null if no such file exists.
     */
    public File getFile(String fileId) {
        File res = new File(fileDir, fileId);
        
        if(res.isFile()) {
            return res;
        } else {
            return null;
        }
    }
    
    /**
     * @param fileId The file id to test whether it exists.
     * @return Whether the given file exists within the archive.
     */
    public boolean hasFile(String fileId) {
        return new File(fileDir, fileId).isFile();
    }
    
    /**
     * @return Retrieves the list of archived files.
     */
    public List<String> getFileIds() {
        return Arrays.asList(fileDir.list());
    }
    
    /**
     * @return The number of bytes left for the base directory.
     */
    public Long getBytesLeft() {
        return baseDepositDir.getFreeSpace();
    }
    
    /**
     * @param fileId The id of the file
     * @return A new file in the temporary directory.
     */
    public File getFileInTempDir(String fileId) {
        File res = new File(tmpDir, fileId);
        if(res.exists()) {
            throw new IllegalStateException("Cannot create a new file in the temporary directory.");
        }
        return res;
    }
    
    /**
     * @param fileId The id of the file.
     * @return Whether a given file exist in the temporary directory.
     */
    public boolean hasFileInTempDir(String fileId) {
        return new File(tmpDir, fileId).isFile();
    }
    
    /**
     * Moves a file from the tmpDir to the archive dir.
     * @param fileId The id of the file to 
     */
    public void moveFromTmpToArchive(String fileId) {
        File tmpFile = new File(tmpDir, fileId);
        File archiveFile = new File(fileDir, fileId);
        
        if(!tmpFile.isFile()) {
            throw new IllegalStateException("The file '" + fileId + "' does not exist within the tmpDir.");
        }
        if(archiveFile.isFile()) {
            throw new IllegalStateException("The file '" + fileId + "' does already exist within the fileDir.");
        }
        
        // Move the file to the fileDir.
        FileUtils.moveFile(tmpFile, archiveFile);
    }
    
    /**
     * The file to deprecate/delete. The file is just moved from the fileDir to the retainDir. 
     * @param fileId The id of the file to deprecate.
     */
    public void deprecateFile(String fileId) {
        File oldFile = new File(fileDir, fileId);
        if(!oldFile.isFile()) {
            throw new IllegalStateException("Cannot locate the file to delete '" + oldFile.getAbsolutePath() + "'!");
        }
        File retainFile = new File(retainDir, fileId);
        
        // If a version of the file already has been retained, then it should be deprecated.
        if(retainFile.exists()) {
            FileUtils.deprecateFile(retainFile);
        }
        
        FileUtils.moveFile(oldFile, retainFile);
    }
}
