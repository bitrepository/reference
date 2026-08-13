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
package org.bitrepository.integrityservice;

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.integrityservice.alerter.IntegrityAlarmDispatcher;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.protocol.IntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

@ExtendWith(SuiteInfoParameterResolver.class)
public class IntegrityAlerterIT extends IntegrationTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    public void testIntegrityFailed() {
        addDescription("Test the IntegrityFailed method for the IntegrityAlerter");

        addStep("Call the function for integrity failure.", "A integrity alarm should be created.");
        IntegrityAlerter alerter = new IntegrityAlarmDispatcher(settingsForCUT, messageBus, null);
        alerter.integrityFailed("Testaintegrity alarm", collectionID);
        AlarmMessage alarmMessage = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assertions.assertEquals(AlarmCode.INTEGRITY_ISSUE, alarmMessage.getAlarm().getAlarmCode());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    public void testOperationFailed() {
        addDescription("Test the OperationFailed method for the IntegrityAlerter");

        addStep("Call the function for integrity failure.", "A integrity alarm should be generate.");

        IntegrityAlerter alerter = new IntegrityAlarmDispatcher(settingsForCUT, messageBus, null);
        alerter.operationFailed("Testing the ability to fail.", collectionID);

        AlarmMessage alarmMessage = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assertions.assertEquals(AlarmCode.FAILED_OPERATION, alarmMessage.getAlarm().getAlarmCode());
    }
}
