package org.bitrepository.integrityclient;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.bitrepository.integrityclient.scheduler.IntegrityInformationScheduler;
import org.bitrepository.integrityclient.scheduler.triggers.CollectAllChecksumsFromPillarTrigger;
import org.bitrepository.integrityclient.scheduler.triggers.CollectAllFileIDsFromPillarTrigger;
import org.bitrepository.integrityclient.scheduler.triggers.CollectChecksumsTrigger;
import org.bitrepository.protocol.messagebus.MessageBusManager;

/**
 * Simple integrity service.
 */
public class SimpleIntegrityService {
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_CHECKSUM_TRIGGER = "The Checksum Collector Trigger";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_FILEIDS_TRIGGER = "The FileIDs Collector Trigger For Pillar ";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_CHECKSUMS_TRIGGER = "The Checksums Collector Trigger For Pillar ";

    /** The scheduler. */
    private final IntegrityInformationScheduler scheduler;
    /** The information collector. */
    private final IntegrityInformationCollector collector;
    /** The cache.*/
    private final CachedIntegrityInformationStorage cache;
    /** The settings. */
    private final Settings settings;
    
    /**
     * Constructor.
     * @param settings The settings for the service.
     */
    public SimpleIntegrityService(Settings settings) {
        this.settings = settings;
        this.scheduler = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings);
        this.collector = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                MessageBusManager.getMessageBus(settings), settings);
        this.cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
    }
    
    /**
     * Initiates the scheduling of checksum collecting.
     * @param millisSinceLastUpdate The time since last update for a checksum to be calculated.
     * @param intervalBetweenChecks The time between checking for outdated checksums.
     */
    public void scheduleChecksumCollecting(long millisSinceLastUpdate, long intervalBetweenChecks) {
        // Default checksum used.
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        checksumType.setChecksumType(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType());
        
        CollectChecksumsTrigger trigger = new CollectChecksumsTrigger(intervalBetweenChecks, millisSinceLastUpdate, 
                checksumType, collector, cache);
        
        scheduler.addTrigger(trigger, DEFAULT_NAME_OF_CHECKSUM_TRIGGER);
    }
    
    /**
     * Initiates the scheduling of collecting all the file ids from a single pillar.
     * @param pillarId The id of the pillar to collect file ids from.
     * @param intervalBetweenCollecting The time between collecting all the file ids.
     */
    public void scheduleAllFileIDsCollectingFromPillar(String pillarId, long intervalBetweenCollecting) {
        CollectAllFileIDsFromPillarTrigger trigger = new CollectAllFileIDsFromPillarTrigger(
                intervalBetweenCollecting, pillarId, collector);
        
        scheduler.addTrigger(trigger, DEFAULT_NAME_OF_ALL_FILEIDS_TRIGGER + pillarId);
    }

    /**
     * Initiates the scheduling of collecting all the file ids from a single pillar.
     * @param pillarId The id of the pillar to collect file ids from.
     * @param intervalBetweenCollecting The time between collecting all the file ids.
     */
    public void scheduleAllChecksumsCollectingFromPillar(String pillarId, long intervalBetweenCollecting) {
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        checksumType.setChecksumType(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType());
        
        CollectAllChecksumsFromPillarTrigger trigger = new CollectAllChecksumsFromPillarTrigger(
                intervalBetweenCollecting, pillarId, checksumType, collector);
        
        scheduler.addTrigger(trigger, DEFAULT_NAME_OF_ALL_CHECKSUMS_TRIGGER + pillarId);
    }
    
    /**
     * Collects the checksum for a given file on all pillars. 
     * Algorithm and salt are optional and can be used for requiring a recalculation of the checksums.
     * @param fileID The id of the file to collect its checksum for.
     * @param checksumAlgorithm The algorithm to use for the checksum collecting. 
     * If null, then the default from settings is used.
     * @param salt The salt for the checksum calculation. If null or empty string, then no salt is used. 
     * @param auditTrailInformation The information for the audit.
     */
    public void collectChecksums(String fileID, String checksumAlgorithm, String salt, String auditTrailInformation) {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(fileID);
        
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        if(checksumAlgorithm == null || checksumAlgorithm.isEmpty()) {
            checksumType.setChecksumType(
                    settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType());
        } else {
            checksumType.setChecksumType(checksumAlgorithm);
        }
        
        checksumType.setChecksumSalt(salt);
        
        collector.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                fileIDs, checksumType, auditTrailInformation);
    }
}
