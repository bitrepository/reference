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

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class AlarmDatabaseExtractionModelTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void alarmExceptionTest() {
        addDescription("Test the AlarmDatabaseExtractionModel class");
        addStep("Define constants etc.", "Should be OK");
        boolean ascending = true;

        addStep("Create an empty model", "Should be populated with nulls.");
        AlarmDatabaseExtractionModel model =
                new AlarmDatabaseExtractionModel(null, null, null, (Instant) null,
                        (Instant) null, null, null, ascending);

        Assertions.assertNull(model.getAlarmCode());
        Assertions.assertNull(model.getComponentId());
        Assertions.assertNull(model.getEnd());
        Assertions.assertNull(model.getFileID());
        Assertions.assertNull(model.getStart());
        Assertions.assertNull(model.getCollectionID());
        Assertions.assertEquals(ascending, model.getAscending());
        Assertions.assertEquals(Integer.MAX_VALUE, model.getMaxCount().intValue());

        addStep("Test the AlarmCode", "Should be able to put a new one in and extract it again.");
        AlarmCode defaultAlarmCode = AlarmCode.COMPONENT_FAILURE;
        model.setAlarmCode(defaultAlarmCode);
        Assertions.assertEquals(defaultAlarmCode, model.getAlarmCode());

        addStep("Test the ascending", "Should be able to put a new one in and extract it again.");
        boolean defaultAscending = false;
        model.setAscending(defaultAscending);
        Assertions.assertEquals(defaultAscending, model.getAscending());

        addStep("Test the ComponentID", "Should be able to put a new one in and extract it again.");
        String defaultComponentID = "DefaultComponentID";
        model.setComponentId(defaultComponentID);
        Assertions.assertEquals(defaultComponentID, model.getComponentId());

        addStep("Test the EndDate", "Should be able to put a new one in and extract it again.");
        Instant defaultEndDate = Instant.ofEpochMilli(987654321);
        model.setEnd(defaultEndDate);
        Assertions.assertEquals(defaultEndDate, model.getEnd());

        addStep("Test the FileID", "Should be able to put a new one in and extract it again.");
        String defaultFileID = "DefaultFileID";
        model.setFileID(defaultFileID);
        Assertions.assertEquals(defaultFileID, model.getFileID());

        addStep("Test the MaxCount", "Should be able to put a new one in and extract it again.");
        Integer defaultMaxCount = 192837456;
        model.setMaxCount(defaultMaxCount);
        Assertions.assertEquals(defaultMaxCount, model.getMaxCount());

        addStep("Test the StartDate", "Should be able to put a new one in and extract it again.");
        Instant defaultStartDate = Instant.ofEpochMilli(123456789);
        model.setStart(defaultStartDate);
        Assertions.assertEquals(defaultStartDate, model.getStart());

        addStep("Test the CollectionID", "Should be able to put a new one in and extract it again.");
        String collectionID = "collection1";
        model.setCollectionID(collectionID);
        Assertions.assertEquals(collectionID, model.getCollectionID());
    }
}
