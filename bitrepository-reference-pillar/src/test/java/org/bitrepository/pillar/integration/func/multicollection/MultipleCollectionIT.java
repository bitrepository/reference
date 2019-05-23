/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.integration.func.multicollection;

import java.util.Arrays;
import java.util.Collection;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.pillar.integration.func.Assert;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.testng.annotations.Test;

public class MultipleCollectionIT extends PillarIntegrationTest {
    /** Used for receiving responses from the pillar */
    protected MessageReceiver clientReceiver;

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void fileInOtherCollectionTest() throws Exception {
        addDescription("Tests that a file is put correctly to a second collection, and that the file can be access " +
                "with getFile, getChecksums, getFileIDs and can be replaced and deleted correctly.");
        addStep("Put the file to the second collection", "Should complete successfully");
        ChecksumSpecTYPE checksumspec = new ChecksumSpecTYPE();
        checksumspec.setChecksumType(ChecksumType.MD5);
        
        clientProvider.getPutClient().putFile(
                nonDefaultCollectionId, DEFAULT_FILE_URL, NON_DEFAULT_FILE_ID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);

        addStep("Send a getFileIDs for the file in the second collection", "The fileID should be retrieved");
        ContributorQuery query = new ContributorQuery(getPillarID(), null, null, null);
        Assert.assertEquals(1, clientProvider.getGetFileInfosClient().getFileInfos(
                nonDefaultCollectionId, new ContributorQuery[] {query}, NON_DEFAULT_FILE_ID, checksumspec, DEFAULT_FILE_URL, null, null).size());

        addStep("Send a getFileIDs for the file in the other collections", "The file should not be found here");
        try {
            clientProvider.getGetFileInfosClient().getFileInfos(
                    collectionID, new ContributorQuery[] {query}, NON_DEFAULT_FILE_ID, checksumspec, DEFAULT_FILE_URL, null, null).size();
            Assert.fail("Should have throw a NegativeResponseException as the file doesn't exist in the default " +
                    "collection");
        } catch (NegativeResponseException nre){
            //Expected
        }
    }

    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();

        clientReceiver = new MessageReceiver(settingsForTestClient.getReceiverDestinationID(), testEventManager);
        addReceiver(clientReceiver);

        Collection<String> pillarFilter = Arrays.asList(testConfiguration.getPillarUnderTestID());
        clientReceiver.setFromFilter(pillarFilter);
        alarmReceiver.setFromFilter(pillarFilter);
    }
}
