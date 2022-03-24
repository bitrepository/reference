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
package org.bitrepository.pillar.integration.func.getchecksums;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;

public class GetChecksumQueryTest extends PillarFunctionTest {
    
    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST} )
    public void checksumSortingTest() {
        addDescription("Test whether the checksum result is sorted oldest to newest.");
        addFixture("Ensure at least two files are present on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Retrieve a list of all checksums.",
            "Run through the list and verify each element is older or the same age as the following element");
        List<ChecksumDataForChecksumSpecTYPE> originalChecksumList = pillarFileManager.getChecksums(null, null, null);
        
        for (int counter = 0 ; counter < originalChecksumList.size() - 1 ; counter ++) {
            Assert.assertTrue(originalChecksumList.get(counter).getCalculationTimestamp().compare(
                    originalChecksumList.get(counter + 1).getCalculationTimestamp()) <= 0,
                    "Checksum (" + counter + ") " + originalChecksumList.get(counter) + " newer than following checksum("
                            + counter + ") " + originalChecksumList.get(counter + 1));
        }
    }

    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST} )
    public void maxNumberOfResultTest() {
        addDescription("Verifies the size of the result set can be limited by setting the maxNumberOfResult parameter.");
        addFixture("Ensure at least two files are present on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        
        addStep("Retrieve a list of all checksums by setting maxNumberOfResult to null.", "At least 2 checksums " +
                "should be returned");
        List<ChecksumDataForChecksumSpecTYPE> originalChecksumList = pillarFileManager.getChecksums(null, null, null);
        
        addStep("Repeat the request checksums, this time with maxNumberOfResult set to one", "A checksum result with " +
                "a single checksum should be returned. The checksum should be the oldest/first checksum in the full list.");
        ContributorQuery singleChecksumQuery = new ContributorQuery(getPillarID(), null, null, 1);
        List<ChecksumDataForChecksumSpecTYPE> singleChecksumList = pillarFileManager.getChecksums(null,
                singleChecksumQuery, null);
        Assert.assertEquals(singleChecksumList.size(), 1, "The result didn't contain a single checksum");
        Assert.assertEquals(singleChecksumList.get(0), originalChecksumList.get(0),
                "The returned checksum wasn't equal to the oldest checksum");
    }
    
    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST} )
    public void minTimeStampTest() {
        addDescription("Test the pillar support for only retrieving checksums newer that a given time. " +
                "Note that this test assumes there is at least 2 checksums with different timestamps." +
                "(Checksum lists are not compared directly as long lists might have checksums with shared " +
                "timestamps which possibly leads to ambiguous results when sorting by time only. " +
                "Instead, list size and outer points are compared)");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        
        addStep("Request default checksums for all files on the pillar",
                "A list with at least 2 different timestamps (it is not the fault of the pillar if this fails, but " +
                "the test needs this to be satisfied to make sense).");
        List<ChecksumDataForChecksumSpecTYPE> originalChecksumList = pillarFileManager.getChecksums(null, null, null);
        Assert.assertTrue(originalChecksumList.get(0).getCalculationTimestamp().compare(
                originalChecksumList.get(originalChecksumList.size()-1).getCalculationTimestamp()) != 0,
                "The timestamps of the first and last checksum are the same.");
        
        addStep("Request checksums with MinTimeStamp set to the timestamp of the oldest checksum",
                "All checksums should be returned.");
        XMLGregorianCalendar oldestTimestamp = originalChecksumList.get(0).getCalculationTimestamp();
        ContributorQuery query = new ContributorQuery(getPillarID(),
                oldestTimestamp.toGregorianCalendar().getTime(), null, null);
        List<ChecksumDataForChecksumSpecTYPE> limitedChecksumList = pillarFileManager.getChecksums(null, query, null);
        Assert.assertEquals(limitedChecksumList.size(), originalChecksumList.size(),
                "Differing size of checksum lists");
        Assert.assertEquals(limitedChecksumList.get(0), originalChecksumList.get(0),
                "Different first list element when setting oldest minTimestamp");
        Assert.assertEquals(limitedChecksumList.get(limitedChecksumList.size()-1), originalChecksumList.get(originalChecksumList.size()-1),
                "Different last list element when setting oldest minTimestamp");
        Assert.assertTrue(limitedChecksumList.get(0).getCalculationTimestamp().compare(
                limitedChecksumList.get(limitedChecksumList.size()-1).getCalculationTimestamp()) <= 0,
                "First checksum has newer timestamp than last checksum");

        addStep("Request checksums with MinTimeStamp set to the timestamp of the newest checksum",
                "Only checksum with the timestamp equal to MinTimeStamp are returned.");
        XMLGregorianCalendar newestTimestamp = originalChecksumList.get(originalChecksumList.size()-1).getCalculationTimestamp();
        query = new ContributorQuery(getPillarID(), newestTimestamp.toGregorianCalendar().getTime(), null, null);
        limitedChecksumList = pillarFileManager.getChecksums(null, query, null);
        Assert.assertFalse(limitedChecksumList.isEmpty(),
                "Empty list returned when when minTimestamp is set to newest calculated checksum timestamp");
        Assert.assertTrue(limitedChecksumList.get(0).getCalculationTimestamp().compare(newestTimestamp) == 0,
                "Different timestamps in the set of newest checksums." + limitedChecksumList);
        
        addStep("Request checksums with MinTimeStamp set to the timestamp of the newest checksum + 10 ms",
                "No checksums are returned.");
        GregorianCalendar newerThanNewestTimestamp = newestTimestamp.toGregorianCalendar();
        newerThanNewestTimestamp.add(GregorianCalendar.MILLISECOND, 10);
        query = new ContributorQuery(getPillarID(), newerThanNewestTimestamp.getTime(), null, null);
        limitedChecksumList = pillarFileManager.getChecksums(null, query, null);
        Assert.assertEmpty(limitedChecksumList,
                "Non-empty checksum list returned with newerThanNewestTimestamp(" +
                        CalendarUtils.getXmlGregorianCalendar(newerThanNewestTimestamp) + ") query");
    }
    
    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST} )
    public void maxTimeStampTest() {
        addDescription("Test the pillar support for only retrieving checksums older than a given time. " +
                "Note that this test assumes there is at least 2 checksums with different timestamps. " +
                "(Checksum lists are not compared directly as long lists might have checksums with shared " +
                "timestamps which possibly leads to ambiguous results when sorting by time only. " +
                "Instead, list size and outer points are compared)");

        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        
        addStep("Request default checksums for all files on the pillar",
                "A list with at least 2 different timestamps (it is not the fault of the pillar if this fails, but " +
                "the test needs this to be satisfied to make sense).");
        List<ChecksumDataForChecksumSpecTYPE> originalChecksumList = pillarFileManager.getChecksums(null, null, null);
        Assert.assertTrue(originalChecksumList.get(0).getCalculationTimestamp().compare(
                originalChecksumList.get(originalChecksumList.size()-1).getCalculationTimestamp()) != 0,
                "The timestamps of the first and last checksum are the same.");
        
        addStep("Request checksums with MaxTimeStamp set to the timestamp of the newest checksum",
                "All checksums should be returned.");
        XMLGregorianCalendar newestTimestamp = originalChecksumList.get(originalChecksumList.size()-1).getCalculationTimestamp();
        ContributorQuery query = new ContributorQuery(getPillarID(),
                null, newestTimestamp.toGregorianCalendar().getTime(), null);
        List<ChecksumDataForChecksumSpecTYPE> limitedChecksumList = pillarFileManager.getChecksums(null, query, null);
        Assert.assertEquals(limitedChecksumList.size(), originalChecksumList.size(),
                "Differing size of checksum lists");
        Assert.assertEquals(limitedChecksumList.get(0), originalChecksumList.get(0),
                "Different first list element when setting newest maxTimestamp");
        Assert.assertEquals(limitedChecksumList.get(limitedChecksumList.size()-1), originalChecksumList.get(originalChecksumList.size()-1),
                "Different last list element when setting newest maxTimestamp");
        Assert.assertTrue(limitedChecksumList.get(0).getCalculationTimestamp().compare(
                limitedChecksumList.get(limitedChecksumList.size()-1).getCalculationTimestamp()) <= 0,
                "First checksum has newer timestamp than last checksum");
        
        addStep("Request checksums with MaxTimeStamp set to the timestamp of the oldest checksum",
                "Only checksum with the timestamp equal to MaxTimeStamp are returned.");
        XMLGregorianCalendar oldestTimestamp = originalChecksumList.get(0).getCalculationTimestamp();
        query = new ContributorQuery(getPillarID(),
                null, oldestTimestamp.toGregorianCalendar().getTime(), null);
        limitedChecksumList = pillarFileManager.getChecksums(null, query, null);
        Assert.assertFalse(limitedChecksumList.isEmpty(), "At least one checksum with the oldest timestamp should be returned.");
        Assert.assertTrue(limitedChecksumList.get(0).getCalculationTimestamp().compare(oldestTimestamp) == 0,
                "Different timestamps in the set of oldest checksums." + limitedChecksumList);
        
        addStep("Request checksums with MaxTimeStamp set to the timestamp of the oldest checksum - 10 ms",
                "No checksums are returned.");
        GregorianCalendar olderThanOldestTimestamp = oldestTimestamp.toGregorianCalendar();
        olderThanOldestTimestamp.add(GregorianCalendar.MILLISECOND, -10);
        query = new ContributorQuery(getPillarID(), null, olderThanOldestTimestamp.getTime(), null);
        limitedChecksumList = pillarFileManager.getChecksums(null, query, null);
        Assert.assertEmpty(limitedChecksumList,
                "Non-empty checksum list returned with olderThanOldestTimestamp(" +
                        CalendarUtils.getXmlGregorianCalendar(olderThanOldestTimestamp) + ") query");
    }          
}
