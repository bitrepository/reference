package org.bitrepository.integrityservice.mocks;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.protocol.eventhandler.EventHandler;

public class MockCollector implements IntegrityInformationCollector {

    public MockCollector() {}

    private int callsForGetFileIDs = 0;
    private int callsForGetChecksums = 0;
    
    @Override
    public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
            EventHandler eventHandler) {
        callsForGetFileIDs++;
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
            String auditTrailInformation, EventHandler eventHandler) {
        callsForGetChecksums++;
    }
    
    public int getNumberOfCallsForGetFileIDs() {
        return callsForGetFileIDs;
    }
    
    public int getNumberOfCallsForGetChecksums() {
        return callsForGetChecksums;
    }
}
