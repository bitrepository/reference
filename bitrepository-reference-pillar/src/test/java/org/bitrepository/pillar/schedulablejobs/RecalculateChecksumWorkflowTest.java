package org.bitrepository.pillar.schedulablejobs;
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

import org.bitrepository.pillar.DefaultPillarTest;
import org.bitrepository.service.workflow.SchedulableJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Date;

public class RecalculateChecksumWorkflowTest extends DefaultPillarTest {

    DatatypeFactory factory;

    @BeforeEach
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testWorkflowRecalculatesChecksum() throws Exception {
        addDescription("Test that the workflow recalculates the workflows, when the maximum age has been met.");
        Date beforeWorkflowDate = csCache.getCalculationDate(defaultFileId, collectionID);
        Assertions.assertEquals(1, csCache.getAllFileIDs(collectionID).size());
        Assertions.assertEquals(1, archives.getAllFileIds(collectionID).size());
        settingsForCUT.getReferenceSettings().getPillarSettings().setMaxAgeForChecksums(factory.newDuration(0));

        synchronized (this) {
            wait(100);
        }

        addStep("Create and run workflow", "The checksum");
        SchedulableJob workflow = new RecalculateChecksumJob(collectionID, model);
        workflow.start();
        Date afterWorkflowDate = csCache.getCalculationDate(defaultFileId, collectionID);

        Assertions.assertTrue(beforeWorkflowDate.getTime() < afterWorkflowDate.getTime(),
                beforeWorkflowDate.getTime() + " < " + afterWorkflowDate.getTime());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testWorkflowDoesNotRecalculateWhenNotNeeded() throws Exception {
        addDescription("Test that the workflow does not recalculates the workflows, when the maximum age has "
                + "not yet been met.");
        Date beforeWorkflowDate = csCache.getCalculationDate(defaultFileId, collectionID);
        Assertions.assertEquals(1, csCache.getAllFileIDs(collectionID).size());
        Assertions.assertEquals(1, archives.getAllFileIds(collectionID).size());
        settingsForCUT.getReferenceSettings().getPillarSettings()
                .setMaxAgeForChecksums(factory.newDuration(Long.MAX_VALUE));

        synchronized (this) {
            wait(100);
        }

        addStep("Create and run workflow", "The checksum");
        SchedulableJob workflow = new RecalculateChecksumJob(collectionID, model);
        workflow.start();
        Date afterWorkflowDate = csCache.getCalculationDate(defaultFileId, collectionID);

        Assertions.assertEquals(beforeWorkflowDate.getTime(), afterWorkflowDate.getTime(),
                beforeWorkflowDate.getTime() + " == " + afterWorkflowDate.getTime());
    }
}
