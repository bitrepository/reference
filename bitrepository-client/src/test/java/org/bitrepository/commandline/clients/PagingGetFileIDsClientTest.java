/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2026 Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.commandline.clients;

import org.bitrepository.TestGroups;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.outputformatter.GetFileIDsOutputFormatter;
import org.bitrepository.common.utils.CalendarUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PagingGetFileIDsClientTest {
    private static final String COLLECTION_ID = "collection";
    private static final int PAGE_SIZE = 5;

    private final GetFileIDsClient client = mock(GetFileIDsClient.class);
    private final PagingGetFileIDsClient pagingClient = new PagingGetFileIDsClient(client, Duration.ofSeconds(1),
            PAGE_SIZE, mock(GetFileIDsOutputFormatter.class), mock(OutputHandler.class));

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void followUpQueriesOnlyCoverPillarsWithPartialResultsAndStartAfterTheirLatestFileID() {
        Instant latestModification = Instant.parse("2024-01-02T03:04:05Z");
        List<ContributorQuery[]> requestedQueries = new ArrayList<>();
        doAnswer(invocation -> {
            requestedQueries.add(invocation.getArgument(1, ContributorQuery[].class));
            EventHandler eventHandler = invocation.getArgument(4, EventHandler.class);
            if (requestedQueries.size() == 1) {
                eventHandler.handleEvent(new FileIDsCompletePillarEvent("pillar1", COLLECTION_ID,
                        fileIDs("file1", latestModification), true));
                eventHandler.handleEvent(new FileIDsCompletePillarEvent("pillar2", COLLECTION_ID,
                        fileIDs("file1", Instant.EPOCH.plusSeconds(10)), false));
            }
            eventHandler.handleEvent(new CompleteEvent(COLLECTION_ID, null));
            return null;
        }).when(client).getFileIDs(eq(COLLECTION_ID), any(ContributorQuery[].class), eq("file1"), isNull(),
                any(EventHandler.class));

        boolean result = pagingClient.getFileIDs(COLLECTION_ID, "file1", List.of("pillar1", "pillar2"));

        assertTrue(result);
        assertEquals(2, requestedQueries.size());
        assertArrayEquals(new ContributorQuery[]{
                new ContributorQuery("pillar1", Instant.EPOCH, null, PAGE_SIZE),
                new ContributorQuery("pillar2", Instant.EPOCH, null, PAGE_SIZE)
        }, requestedQueries.get(0));
        assertArrayEquals(new ContributorQuery[]{
                new ContributorQuery("pillar1", latestModification, null, PAGE_SIZE)
        }, requestedQueries.get(1));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void noPillarsMeansNoRequestsTest() {
        assertTrue(pagingClient.getFileIDs(COLLECTION_ID, null, List.of()));

        verifyNoInteractions(client);
    }

    private ResultingFileIDs fileIDs(String fileID, Instant lastModification) {
        FileIDsDataItem item = new FileIDsDataItem();
        item.setFileID(fileID);
        item.setFileSize(BigInteger.ONE);
        item.setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(lastModification));
        FileIDsDataItems items = new FileIDsDataItems();
        items.getFileIDsDataItem().add(item);
        FileIDsData data = new FileIDsData();
        data.setFileIDsDataItems(items);
        ResultingFileIDs result = new ResultingFileIDs();
        result.setFileIDsData(data);
        return result;
    }
}
