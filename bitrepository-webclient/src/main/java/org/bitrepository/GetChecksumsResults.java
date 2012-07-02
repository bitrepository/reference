/*
 * #%L
 * Bitrepository Webclient
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
package org.bitrepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.utils.HexUtils;

public class GetChecksumsResults {

    private boolean done = false;
    private boolean failed = false;
    /** Maps fileID to list of pillarIDs*/
    private Map<String, Map<String, String>> results;

    public GetChecksumsResults() {
        results = new HashMap<String, Map<String, String>>();
    }

    public void addResultsFromPillar(String pillarID, ResultingChecksums checksums) {
        List<ChecksumDataForChecksumSpecTYPE> items = checksums.getChecksumDataItems();
        for(ChecksumDataForChecksumSpecTYPE item : items) {
            synchronized (this) {
                if(!results.containsKey(item.getFileID())) {
                    Map<String, String> value = new HashMap<String, String>();
                    value.put(pillarID, HexUtils.byteArrayToString(item.getChecksumValue()));
                    results.put(item.getFileID(), value);
                } else {
                    results.get(item.getFileID()).put(pillarID, HexUtils.byteArrayToString(item.getChecksumValue()));
                }
            }
        }
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized void done() {
        done = true;
    }

    public synchronized void failed() {
        failed = true;
    }

    public synchronized boolean hasFailed() {
        return failed;
    }

    public Map<String, Map<String, String>> getResults() {
        return results;
    }
}
