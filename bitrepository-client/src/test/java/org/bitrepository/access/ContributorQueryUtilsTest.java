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
package org.bitrepository.access;

import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContributorQueryUtilsTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void createFullContributorQueryTest() {
        ContributorQuery[] queries = ContributorQueryUtils.createFullContributorQuery(List.of("pillar1", "pillar2"));

        assertEquals(2, queries.length);
        assertEquals("pillar1", queries[0].getComponentID());
        assertEquals("pillar2", queries[1].getComponentID());
        for (ContributorQuery query : queries) {
            assertNull(query.getMinTimestampInstant());
            assertNull(query.getMaxTimestampInstant());
            assertNull(query.getMaxNumberOfResults());
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void createFullContributorQueryWithoutContributorsTest() {
        assertEquals(0, ContributorQueryUtils.createFullContributorQuery(List.of()).length);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getContributorsTest() {
        ContributorQuery[] queries = {
                new ContributorQuery("pillar1", Instant.EPOCH, null, 10),
                new ContributorQuery("pillar2", (Instant) null, null, null),
                new ContributorQuery("pillar1", null, Instant.EPOCH, 5)
        };

        assertEquals(Set.of("pillar1", "pillar2"), Set.copyOf(ContributorQueryUtils.getContributors(queries)));
        assertEquals(2, ContributorQueryUtils.getContributors(queries).size(), "Contributors are not repeated");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getContributorsWithoutQueriesTest() {
        assertEquals(0, ContributorQueryUtils.getContributors(new ContributorQuery[0]).size());
    }
}
