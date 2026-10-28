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
package org.bitrepository.commandline.outputformatter;

import org.bitrepository.TestGroups;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.ChecksumResult;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GetChecksumDistributionFormatterTest {
    private final OutputHandler outputHandler = mock(OutputHandler.class);
    private final GetChecksumDistributionFormatter formatter = new GetChecksumDistributionFormatter(outputHandler);

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void disagreeingPillarsAreGroupedByChecksumTest() {
        ChecksumResult result = new ChecksumResult("file1", "pillar1", "aaa");
        result.addContributor("pillar2", "bbb");
        result.addContributor("pillar3", "aaa");

        formatter.formatResult(List.of(result));

        assertEquals(Map.of("aaa", Set.of("pillar1", "pillar3"), "bbb", Set.of("pillar2")),
                pillarsPerChecksum("file1", 2));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void pillarsWithDifferentChecksumsGetALineEachTest() {
        ChecksumResult result = new ChecksumResult("file1", "pillar1", "aaa");
        result.addContributor("pillar2", "bbb");
        result.addContributor("pillar3", "ccc");

        formatter.formatResult(List.of(result));

        assertEquals(Map.of("aaa", Set.of("pillar1"), "bbb", Set.of("pillar2"), "ccc", Set.of("pillar3")),
                pillarsPerChecksum("file1", 3));
    }

    /**
     * Parses the output lines, which have the format 'count \tchecksum \t[pillars] \tfileID', into the pillars per checksum,
     * and verifies the count and fileID columns on the way.
     */
    private Map<String, Set<String>> pillarsPerChecksum(String expectedFileID, int expectedLineCount) {
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(outputHandler, atLeastOnce()).resultLine(lines.capture());
        assertEquals(expectedLineCount, lines.getAllValues().size());

        Map<String, Set<String>> pillarsPerChecksum = new HashMap<>();
        for (String line : lines.getAllValues()) {
            String[] columns = line.split(" \t");
            String pillarList = columns[2].substring(1, columns[2].length() - 1);
            Set<String> pillars = new HashSet<>(Arrays.asList(pillarList.split(", ")));
            assertEquals(String.valueOf(pillars.size()), columns[0]);
            assertEquals(expectedFileID, columns[3]);
            pillarsPerChecksum.put(columns[1], pillars);
        }
        return pillarsPerChecksum;
    }
}
