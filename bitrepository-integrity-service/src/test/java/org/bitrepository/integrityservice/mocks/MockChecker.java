package org.bitrepository.integrityservice.mocks;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.IntegrityReport;

public class MockChecker implements IntegrityChecker {
    
    public MockChecker() {}
    
    private int callsForCheckFileIDs = 0;
    private int callsForCheckChecksums = 0;
    
    public int getCallsForCheckFileIDs() {
        return callsForCheckFileIDs;
    }

    public int getCallsForCheckChecksums() {
        return callsForCheckChecksums;
    }

    @Override
    public IntegrityReport checkFileIDs(FileIDs fileIDs) {
        callsForCheckFileIDs++;
        return new IntegrityReport();
    }
    
    @Override
    public IntegrityReport checkChecksum(FileIDs fileIDs) {
        callsForCheckChecksums++;
        return new IntegrityReport();
    }
    
}
