/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.store;

import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static java.lang.Integer.MAX_VALUE;
import static org.bitrepository.bitrepositoryelements.AlarmCode.COMPONENT_FAILURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Runs ExtendedTestCase with a regression test.
 */

public class AlarmDatabaseExtractionModelTest extends ExtendedTestCase {
    @Test
    @Tag("regressiontest")
    public void alarmExceptionTest() throws Exception {
        addDescription("Test the AlarmDatabaseExtractionModel class");
        addStep("Define constants etc.", "Should be OK");
        boolean ascending = true;

        addStep("Create an empty model", "Should be populated with nulls.");
        AlarmDatabaseExtractionModel model = new AlarmDatabaseExtractionModel(null, null, null, null, null, null, null, ascending);

        assertNull(model.getAlarmCode());
        assertNull(model.getComponentId());
        assertNull(model.getEndDate());
        assertNull(model.getFileID());
        assertNull(model.getStartDate());
        assertNull(model.getCollectionID());
        assertEquals(ascending, model.getAscending());
        assertEquals(MAX_VALUE, model.getMaxCount().intValue());

        addStep("Test the AlarmCode", "Should be able to put a new one in and extract it again.");
        AlarmCode defaultAlarmCode = COMPONENT_FAILURE;
        model.setAlarmCode(defaultAlarmCode);
        assertEquals(defaultAlarmCode, model.getAlarmCode());

        addStep("Test the ascending", "Should be able to put a new one in and extract it again.");
        boolean defaultAscending = false;
        model.setAscending(defaultAscending);
        assertEquals(defaultAscending, model.getAscending());

        addStep("Test the ComponentID", "Should be able to put a new one in and extract it again.");
        String defaultComponentID = "DefaultComponentID";
        model.setComponentId(defaultComponentID);
        assertEquals(defaultComponentID, model.getComponentId());

        addStep("Test the EndDate", "Should be able to put a new one in and extract it again.");
        Date defaultEndDate = new Date(987654321);
        model.setEndDate(defaultEndDate);
        assertEquals(defaultEndDate, model.getEndDate());

        addStep("Test the FileID", "Should be able to put a new one in and extract it again.");
        String defaultFileID = "DefaultFileID";
        model.setFileID(defaultFileID);
        assertEquals(defaultFileID, model.getFileID());

        addStep("Test the MaxCount", "Should be able to put a new one in and extract it again.");
        Integer defaultMaxCount = 192837456;
        model.setMaxCount(defaultMaxCount);
        assertEquals(defaultMaxCount, model.getMaxCount());

        addStep("Test the StartDate", "Should be able to put a new one in and extract it again.");
        Date defaultStartDate = new Date(123456789);
        model.setStartDate(defaultStartDate);
        assertEquals(defaultStartDate, model.getStartDate());

        addStep("Test the CollectionID", "Should be able to put a new one in and extract it again.");
        String collectionID = "collection1";
        model.setCollectionID(collectionID);
        assertEquals(collectionID, model.getCollectionID());
    }
}
