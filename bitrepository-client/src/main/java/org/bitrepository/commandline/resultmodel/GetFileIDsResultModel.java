/*
 * #%L
 * Bitrepository Command Line
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.commandline.resultmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Model for keeping results from a GetFileIDs call. The model is intended to act as a buffer for
 * completed and uncompleted data. The intension is that completed data can be fetched while the 
 * remaining data is still being fetched - this should serve to keep memory use down. 
 */
public class GetFileIDsResultModel {

    private List<FileIDsResult> completeResults;
    private Set<String> lastCompletedIDs;
    private Map<String, FileIDsResult> uncompleteResults;
    private Map<String, Date> latestContributorDate;
        
    public GetFileIDsResultModel(Collection<String> expectedContributors) {
        latestContributorDate = new HashMap<String, Date>();
        for(String contributor : expectedContributors) {
            latestContributorDate.put(contributor, new Date(0));
        }
        completeResults = new ArrayList<FileIDsResult>();
        lastCompletedIDs = new HashSet<String>();
        uncompleteResults = new HashMap<String, FileIDsResult>();
    }
    
    /**
     * Add a set of results from a contributor
     * @param contributor, the contributor from which the results are from
     * @param results, the results from the contributor. 
     */
    public void addResults(String contributor, ResultingFileIDs results) {
        Date latestContribution = latestContributorDate.get(contributor);
        for(FileIDsDataItem item : results.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem()) {
            if(lastCompletedIDs.contains(item.getFileID())) {
                continue;
            }
            FileIDsResult result;
            if(uncompleteResults.containsKey(item.getFileID())) {
                result = uncompleteResults.get(item.getFileID());
                result.updateSize(item.getFileSize());
                result.addContributor(contributor);
            } else {
                result = new FileIDsResult(item.getFileID(), item.getFileSize(), contributor);
            }
            
            Date resultDate = CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()); 
            if(resultDate.after(latestContribution)) {
                latestContribution = resultDate;
            }
                        
            if(result.isComplete(latestContributorDate.size())) {
                completeResults.add(result);
                uncompleteResults.remove(item.getFileID());
            } else {
                uncompleteResults.put(item.getFileID(), result);
            }
        }       
        latestContributorDate.put(contributor, latestContribution);
    }
    
    /**
     * Get the collection of completed results (results from which all expected contributors 
     * delivered their part), the call is NOT idempotent. 
     * @return the collection of {@link FileIDsResult}s 
     */    
    public Collection<FileIDsResult> getCompletedResults() {
        List<FileIDsResult> completed = completeResults;
        completeResults = null;
        completeResults = new ArrayList<FileIDsResult>();
        lastCompletedIDs = new HashSet<String>();
        for(FileIDsResult result : completed) {
            lastCompletedIDs.add(result.getID());
        }
        return completed;
    }

    /**
     * Get the collection of uncompleted results (the results which does not have had contributions 
     * from all expected contributors)
     * @return the collection of {@link FileIDsResult} 
     */    
    public Collection<FileIDsResult> getUncompletedResults() {
        return uncompleteResults.values();
    }
    
    /**
     * Get the Date of the latest fileID by the contributor
     * @param contributor, the contributor to get the Date of the latest contribution.
     * @return Date, the date of the latest contribution by the given contributor
     */
    public Date getLatestContribution(String contributor) {
        return latestContributorDate.get(contributor);
    }
}
