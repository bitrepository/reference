package org.bitrepository.pillar.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.cache.database.ChecksumExtractor;
import org.bitrepository.pillar.cache.database.ChecksumIngestor;

/**
 * The checksum store backed by a database.
 */
public class ChecksumDAO implements ChecksumStore {
    /** The ingestor for the database.*/
    private final ChecksumIngestor ingestor;
    /** The extractor for the database.*/
    private final ChecksumExtractor extractor;
    /** The connector for the database.*/
    private final DBConnector connector;
    
    /**
     * Constructor.
     * @param settings The settings.
     */
    public ChecksumDAO(Settings settings) {
        connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());
        
        this.ingestor = new ChecksumIngestor(connector);
        this.extractor = new ChecksumExtractor(connector);
    }
    
    @Override
    public void insertChecksumCalculation(String fileId, String checksum, Date calculationDate) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        
        if(extractor.hasFile(fileId)) {
            ingestor.updateEntry(fileId, checksum, calculationDate);
        } else {
            ingestor.insertNewEntry(fileId, checksum, calculationDate);
        }
    }

    @Override
    public void deleteEntry(String fileId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");

        if(!extractor.hasFile(fileId)) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        ingestor.removeEntry(fileId);
    }

    @Override
    public ChecksumEntry getEntry(String fileId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        
        return extractor.extractSingleEntry(fileId);
    }
    
    @Override
    public List<ChecksumEntry> getAllEntries() {
        return extractor.extractAllEntries();
    }
    
    @Override
    public Date getCalculationDate(String fileId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");

        Date res = extractor.extractDateForFile(fileId);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        return res;
        
    }
    
    @Override
    public Collection<String> getFileIDs() {
        return extractor.getAllFileIDs();
    }

    @Override
    public boolean hasFile(String fileId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");

        return extractor.hasFile(fileId);
    }

    @Override
    public String getChecksum(String fileId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");

        String res = extractor.extractChecksumForFile(fileId);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        return res;
    }

    @Override
    public void close() {
        connector.destroy();
    }
}
