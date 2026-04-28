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
package org.bitrepository.integrityservice.workflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

class IntegrityContributorsTest {

    private final static String PILLAR1 = "pillar1";
    private final static String PILLAR2 = "pillar2";

    @Test
    @Tag("regressiontest")
    void testConstructor() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2), 3);
        Set<String> activeContributors = ic.getActiveContributors();
        Assertions.assertTrue(activeContributors.contains(PILLAR1));
        Assertions.assertTrue(activeContributors.contains(PILLAR2));
        Assertions.assertTrue(ic.getFailedContributors().isEmpty());
        Assertions.assertTrue(ic.getFinishedContributors().isEmpty());
    }

    @Test
    @Tag("regressiontest")
    void testFailContributor() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2), 1);
        ic.failContributor(PILLAR1);
        Assertions.assertTrue(ic.getFailedContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getActiveContributors().contains(PILLAR2));
        Assertions.assertTrue(ic.getFinishedContributors().isEmpty());
    }

    @Test
    @Tag("regressiontest")
    void testRetry() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2), 3);
        ic.failContributor(PILLAR1);
        Assertions.assertTrue(ic.getActiveContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getFailedContributors().isEmpty());
        ic.failContributor(PILLAR1);
        Assertions.assertTrue(ic.getActiveContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getFailedContributors().isEmpty());
        ic.failContributor(PILLAR1);
        Assertions.assertFalse(ic.getActiveContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getFailedContributors().contains(PILLAR1));
    }

    @Test
    @Tag("regressiontest")
    void testSucceed() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2), 2);
        ic.failContributor(PILLAR1);
        ic.failContributor(PILLAR2);
        Assertions.assertTrue(ic.getActiveContributors().containsAll(Arrays.asList(PILLAR1, PILLAR2)));
        ic.succeedContributor(PILLAR1);
        ic.failContributor(PILLAR1);
        ic.failContributor(PILLAR2);
        Assertions.assertTrue(ic.getActiveContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getFailedContributors().contains(PILLAR2));

    }

    @Test
    @Tag("regressiontest")
    void testFinishContributor() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2), 3);
        ic.finishContributor(PILLAR1);
        Assertions.assertTrue(ic.getFinishedContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getActiveContributors().contains(PILLAR2));
        Assertions.assertTrue(ic.getFailedContributors().isEmpty());
    }

    @Test
    @Tag("regressiontest")
    void testReloadContributors() {
        IntegrityContributors ic = new IntegrityContributors(Arrays.asList(PILLAR1, PILLAR2), 1);
        ic.finishContributor(PILLAR1);
        ic.failContributor(PILLAR2);
        Assertions.assertTrue(ic.getActiveContributors().isEmpty());
        Assertions.assertTrue(ic.getFinishedContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getFailedContributors().contains(PILLAR2));

        ic.reloadActiveContributors();
        Assertions.assertTrue(ic.getFinishedContributors().isEmpty());
        Assertions.assertTrue(ic.getActiveContributors().contains(PILLAR1));
        Assertions.assertTrue(ic.getFailedContributors().contains(PILLAR2));
    }

}
