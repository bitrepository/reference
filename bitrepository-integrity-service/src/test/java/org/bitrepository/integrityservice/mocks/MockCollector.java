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

import java.util.Collection;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;

public class MockCollector implements IntegrityInformationCollector {

    public MockCollector() {}

    private int callsForGetFileIDs = 0;
    private int callsForGetChecksums = 0;
    
    @Override
    public void getFileIDs(Collection<String> pillarIDs, String auditTrailInformation, ContributorQuery[] queries,
            EventHandler eventHandler) {
        callsForGetFileIDs++;
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, ChecksumSpecTYPE checksumType, String auditTrailInformation,
            ContributorQuery[] queries, EventHandler eventHandler) {
        callsForGetChecksums++;
    }
    
    public int getNumberOfCallsForGetFileIDs() {
        return callsForGetFileIDs;
    }
    
    public int getNumberOfCallsForGetChecksums() {
        return callsForGetChecksums;
    }
}
