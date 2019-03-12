package org.bitrepository.pillar.store.hadooparchive;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection managed HDFS file store.
 */
public class CollectionHdfsManager implements FileStore {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The settings. */
    protected final Settings settings;

    /** The archives. Mapped between the collections and the hdfs archive.*/
    protected final Map<String, HdfsArchive> archives;

    /** The HDFS file system.*/
    protected final FileSystem fileSystem;

    /**
     * Constructor.
     * @param settings The settings.
     */
    public CollectionHdfsManager(Settings settings) {
        this.settings = settings;

        try {
            Configuration conf = new Configuration();
            fileSystem = FileSystem.get(conf);
            // TODO: make a setting for this root path. And perhaps other hadoop stuff.
            Path rootPath = new Path(".");

            this.archives = initialiseCollections(SettingsUtils.getCollectionIDsForPillar(settings.getComponentID()),
                    rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot instantiate the HDFS archives", e);
        }
    }

    /**
     * Initialises the HDFS archives for each collection.
     * @param collections The list of collections for this pillar.
     * @return The map between the collections and their HDFS archives.
     */
    protected Map<String, HdfsArchive> initialiseCollections(List<String> collections, Path rootPath) {
        Map<String, HdfsArchive> res = new HashMap<>();
        for (String collection : collections) {
            res.put(collection, new HdfsArchive(collection, fileSystem, rootPath));
        }
        return res;
    }

    @Override
    public FileInfo getFileInfo(String fileID, String collectionID) {
        return getArchive(collectionID).getFileInfo(fileID);
    }

    @Override
    public boolean hasFile(String fileID, String collectionID) {
        return getArchive(collectionID).hasFile(fileID);
    }

    @Override
    public Collection<String> getAllFileIds(String collectionID) {
        return getArchive(collectionID).getAllFileIds();
    }

    @Override
    public FileInfo downloadFileForValidation(String fileID, String collectionID, InputStream inputStream)
            throws IOException {
        return getArchive(collectionID).downloadFileForValidation(fileID, inputStream);
    }

    @Override
    public void moveToArchive(String fileID, String collectionID) {
        getArchive(collectionID).moveToArchive(fileID);
    }

    @Override
    public void deleteFile(String fileID, String collectionID) {
        getArchive(collectionID).deleteFile(fileID);
    }

    @Override
    public void replaceFile(String fileID, String collectionID) {
        getArchive(collectionID).replaceFile(fileID);
    }

    @Override
    public long sizeLeftInArchive(String collectionID) {
        return getArchive(collectionID).sizeLeftInArchive();
    }

    @Override
    public FileInfo getFileInTmpDir(String fileID, String collectionID) {
        return getArchive(collectionID).getFileInTmpDir(fileID);
    }

    @Override
    public void ensureFileNotInTmpDir(String fileID, String collectionID) {
        getArchive(collectionID).ensureFileNotInTmpDir(fileID);
    }

    @Override
    public void close() {
        try {
            fileSystem.close();
        } catch (IOException e) {
            log.warn("Error when trying to close.", e);
        }
    }

    /**
     * @param collectionID The ID of the collection.
     * @return The HDFS archive for the given collection.
     */
    protected HdfsArchive getArchive(String collectionID) {
        if(!archives.containsKey(collectionID)) {
            throw new IllegalStateException("No HDFS archive for collection '" + collectionID + "'");
        }
        return archives.get(collectionID);
    }
}
