/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.integrityservice.mocks;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.reports.ChecksumReport;
import org.bitrepository.integrityservice.checking.reports.IntegrityReport;
import org.bitrepository.integrityservice.checking.reports.MissingChecksumReport;
import org.bitrepository.integrityservice.checking.reports.MissingFileReport;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReport;

public class MockChecker implements IntegrityChecker {
    
    public MockChecker() {}
    
    private int callsForCheckFileIDs = 0;
    private int callsForCheckChecksums = 0;
    private int callsForCheckMissingChecksums = 0;
    private int callsForCheckObsoleteChecksums = 0;
    
    public int getCallsForCheckFileIDs() {
        return callsForCheckFileIDs;
    }

    public int getCallsForCheckChecksums() {
        return callsForCheckChecksums;
    }

    public int getCallsForCheckMissingChecksums() {
        return callsForCheckMissingChecksums;
    }

    public int getCallsForCheckObsoleteChecksums() {
        return callsForCheckObsoleteChecksums;
    }

    @Override
    public IntegrityReport checkFileIDs(FileIDs fileIDs) {
        callsForCheckFileIDs++;
        return new MissingFileReport();
    }
    
    @Override
    public IntegrityReport checkChecksum(FileIDs fileIDs) {
        callsForCheckChecksums++;
        return new ChecksumReport();
    }

    @Override
    public IntegrityReport checkMissingChecksums() {
        callsForCheckMissingChecksums++;
        return new MissingChecksumReport();
    }

    @Override
    public IntegrityReport checkObsoleteChecksums(long outdatedInterval) {
        callsForCheckObsoleteChecksums++;
        return new ObsoleteChecksumReport();
    }
}
