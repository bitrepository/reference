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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GetChecksumDistributionFormatterTest {
    private final GetChecksumDistributionFormatter formatter =
            new GetChecksumDistributionFormatter(mock(OutputHandler.class));

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void disagreeingPillarsAreGroupedByChecksumTest() {
        ChecksumResult result = new ChecksumResult("file1", "pillar1", "aaa");
        result.addContributor("pillar2", "bbb");
        result.addContributor("pillar3", "aaa");

        assertEquals(Map.of("aaa", Set.of("pillar1", "pillar3"), "bbb", Set.of("pillar2")),
                pillarsPerChecksum(result));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void pillarsWithDifferentChecksumsGetAGroupEachTest() {
        ChecksumResult result = new ChecksumResult("file1", "pillar1", "aaa");
        result.addContributor("pillar2", "bbb");
        result.addContributor("pillar3", "ccc");

        assertEquals(Map.of("aaa", Set.of("pillar1"), "bbb", Set.of("pillar2"), "ccc", Set.of("pillar3")),
                pillarsPerChecksum(result));
    }

    /** The pillars within a checksum have no defined order, so they are compared as sets. */
    private Map<String, Set<String>> pillarsPerChecksum(ChecksumResult result) {
        return formatter.retrieveChecksumDistribution(result).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
    }
}
