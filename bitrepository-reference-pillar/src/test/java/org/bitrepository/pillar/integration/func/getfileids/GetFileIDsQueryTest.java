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
package org.bitrepository.pillar.integration.func.getfileids;

import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.testng.annotations.Test;

public class GetFileIDsQueryTest extends PillarFunctionTest {

    @Test ( groups = {"pillar-integration-test"} )
    public void fileidsSortingTest() {
        addDescription("Test whether the file id result is sorted oldest to newest.");
        addFixtureSetup("Ensure at least two files are present on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Retrieve a list of all file ids.", "Run through the list and verify each element is older or the " +
                "same age as the following element");
        List<FileIDsDataItem> originalFileIDsList = pillarFileManager.getFileIDs(null);
        Assert.assertTrue(originalFileIDsList.size() >= 2, "Must initially have at least two file ids, but had: "
                + originalFileIDsList.size());

        for (int counter = 0 ; counter < originalFileIDsList.size() - 1 ; counter ++) {
            Assert.assertTrue(originalFileIDsList.get(counter).getLastModificationTime().compare(
                    originalFileIDsList.get(counter + 1).getLastModificationTime()) <= 0,
                    "file id (" + counter + ") " + originalFileIDsList.get(counter) + " newer than following file id("
                            + counter + ") " + originalFileIDsList.get(counter + 1));
        }
    }

    @Test ( groups = {"pillar-integration-test"} )
    public void maxNumberOfResultTest() {
        addDescription("Verifies the size of the result set can be limited by setting the maxNumberOfResult parameter.");
        addFixtureSetup("Ensure at least two files are present on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Retrieve a list of all file ids by setting maxNumberOfResult to null.", "At least 2 file ids " +
                "should be returned");
        List<FileIDsDataItem> originalFileIDsList = pillarFileManager.getFileIDs(null);
        Assert.assertTrue(originalFileIDsList.size() >= 2, "Must initially have at least two file ids, but had: "
                + originalFileIDsList.size());

        addStep("Repeat the request file ids, this time with maxNumberOfResult set to one", "A file id result with " +
                "a single file id should be returned. The file id should be the oldest/first file id in the full list.");
        ContributorQuery singleFileIDQuery = new ContributorQuery(getPillarID(), null, null, 1);
        List<FileIDsDataItem> singleFileIDList = pillarFileManager.getFileIDs(singleFileIDQuery);
        Assert.assertEquals(singleFileIDList.size(), 1, "The result didn't contain a single file id");
        Assert.assertEquals(singleFileIDList.get(0), originalFileIDsList.get(0),
                "The returned file id wasn't equal to the oldest file id");
    }

    @Test ( groups = {"pillar-integration-test"} )
    public void minTimeStampTest() {
        addDescription("Test the pillar support for only retrieving file ids newer that a given time. " +
                "Note that this test assumes there is at least 2 file ids with different timestamps.");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request default file ids for all files on the pillar",
                "A list with at least 2 different timestamps (it is not the fault of the pillar if this fails, but " +
                        "the test needs this to be satisfied to make sense).");
        List<FileIDsDataItem> originalFileIDsList = pillarFileManager.getFileIDs(null);
        Assert.assertTrue(originalFileIDsList.size() >= 2, "Must initially have at least two file ids, but had: "
                + originalFileIDsList.size());
        Assert.assertTrue(originalFileIDsList.get(0).getLastModificationTime().compare(
                originalFileIDsList.get(originalFileIDsList.size()-1).getLastModificationTime()) != 0,
                "The timestamps of the first and last file id are the same.");

        addStep("Request file ids with MinTimeStamp set to the timestamp of the oldest file id",
                "All file ids should be returned.");
        XMLGregorianCalendar oldestTimestamp = originalFileIDsList.get(0).getLastModificationTime();
        ContributorQuery query = new ContributorQuery(getPillarID(),
                oldestTimestamp.toGregorianCalendar().getTime(), null, null);
        List<FileIDsDataItem> limitedFileIDsList = pillarFileManager.getFileIDs(query);
        Assert.assertEquals(limitedFileIDsList, originalFileIDsList, "Different list return when setting old minTimestamp");

        addStep("Request file ids with MinTimeStamp set to the timestamp of the newest file id",
                "Only file id with the timestamp equal to MinTimeStamp are returned.");
        XMLGregorianCalendar newestTimestamp = originalFileIDsList.get(originalFileIDsList.size()-1).getLastModificationTime();
        query = new ContributorQuery(getPillarID(),
                newestTimestamp.toGregorianCalendar().getTime(), null, null);
        limitedFileIDsList = pillarFileManager.getFileIDs(query);
        Assert.assertTrue(limitedFileIDsList.get(0).getLastModificationTime().compare(newestTimestamp) == 0,
                "Different timestamps in the set of newest file ids." + limitedFileIDsList);

        addStep("Request file ids with MinTimeStamp set to the timestamp of the newest file id + 10 ms",
                "No file ids are returned.");
        GregorianCalendar newerThanNewestTimestamp = newestTimestamp.toGregorianCalendar();
        newerThanNewestTimestamp.add(GregorianCalendar.MILLISECOND, 10);
        query = new ContributorQuery(getPillarID(), newerThanNewestTimestamp.getTime(), null, null);
        limitedFileIDsList = pillarFileManager.getFileIDs(query);
        Assert.assertTrue(limitedFileIDsList.isEmpty(),
                "Not empty file id list returned with newerThanNewestTimestamp query.");
        Assert.assertEmpty(limitedFileIDsList, "Non-empty list returned with olderThanOldestTimestamp(" +
                CalendarUtils.getXmlGregorianCalendar(newerThanNewestTimestamp) + ") query");
    }

    @Test ( groups = {"pillar-integration-test"} )
    public void maxTimeStampTest() {
        addDescription("Test the pillar support for only retrieving file ids older that a given time. " +
                "Note that this test assumes there is at least 2 file ids with different timestamps.");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request default file ids for all files on the pillar",
                "A list with at least 2 different timestamps (it is not the fault of the pillar if this fails, but " +
                        "the test needs this to be satisfied to make sense).");
        List<FileIDsDataItem> originalFileIDsList = pillarFileManager.getFileIDs(null);
        Assert.assertTrue(originalFileIDsList.size() >= 2, "Must initially have at least two file ids, but had: "
                + originalFileIDsList.size());
        Assert.assertTrue(originalFileIDsList.get(0).getLastModificationTime().compare(
                originalFileIDsList.get(originalFileIDsList.size()-1).getLastModificationTime()) != 0,
                "The timestamps of the first and last file id are the same.");

        addStep("Request file ids with MaxTimeStamp set to the timestamp of the newest file id",
                "All file ids should be returned.");
        XMLGregorianCalendar newestTimestamp = originalFileIDsList.get(originalFileIDsList.size()-1).getLastModificationTime();
        ContributorQuery query = new ContributorQuery(getPillarID(),
                null, newestTimestamp.toGregorianCalendar().getTime(), null);
        List<FileIDsDataItem> limitedFileIDsList = pillarFileManager.getFileIDs(query);
        Assert.assertEquals(limitedFileIDsList, originalFileIDsList,
                "Different list return when setting newest maxTimestamp");

        addStep("Request file ids with MaxTimeStamp set to the timestamp of the oldest file id",
                "Only file id with the timestamp equal to MaxTimeStamp are returned.");
        XMLGregorianCalendar oldestTimestamp = originalFileIDsList.get(0).getLastModificationTime();
        query = new ContributorQuery(getPillarID(),
                null, oldestTimestamp.toGregorianCalendar().getTime(), null);
        limitedFileIDsList = pillarFileManager.getFileIDs(query);
        Assert.assertTrue(!limitedFileIDsList.isEmpty(), "At least one file id with the oldest timestamp should be " +
                "returned. The folliwing fileIDs where received: ");
        Assert.assertTrue(limitedFileIDsList.get(0).getLastModificationTime().compare(oldestTimestamp) == 0,
                "Different timestamps in the set of oldest file ids." + limitedFileIDsList);

        addStep("Request file ids with MaxTimeStamp set to the timestamp of the oldest file id - 10 ms",
                "No file ids are returned.");
        GregorianCalendar olderThanOldestTimestamp = oldestTimestamp.toGregorianCalendar();
        olderThanOldestTimestamp.add(GregorianCalendar.MILLISECOND, -10);
        query = new ContributorQuery(getPillarID(), null, olderThanOldestTimestamp.getTime(), null);
        limitedFileIDsList = pillarFileManager.getFileIDs(query);
        Assert.assertEmpty(limitedFileIDsList,"Non-empty list returned with olderThanOldestTimestamp(" +
                CalendarUtils.getXmlGregorianCalendar(olderThanOldestTimestamp) + ") query");
    }
}
