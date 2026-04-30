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

package org.bitrepository.pillar.integration.func.getstatus;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.PillarFunctionIT;
import org.bitrepository.pillar.messagefactories.GetStatusMessageFactory;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class GetStatusRequestIT extends PillarFunctionIT {
    protected GetStatusMessageFactory msgFactory;
    private String pillarDestination;

    @BeforeEach
    void initialiseReferenceTest() throws Exception {
        msgFactory = new GetStatusMessageFactory(null, settingsForTestClient, getPillarID(), null);
        pillarDestination = lookupPillarDestination();
        msgFactory = new GetStatusMessageFactory(null, settingsForTestClient, getPillarID(), pillarDestination);
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    void normalGetStatusTest() {
        addDescription("Tests the GetStatus functionality of a pillar for the successful scenario.");

        addStep("Send a GetStatusRequest",
                "The pillar should send a progress response followed by a OK final response.");
        GetStatusRequest request = msgFactory.createGetStatusRequest();
        messageBus.sendMessage(request);

        addStep("Receive and validate the final response", "Should be sent by the pillar.");
        GetStatusFinalResponse finalResponse = clientReceiver.waitForMessage(GetStatusFinalResponse.class);
        Assertions.assertNotNull(finalResponse);
        Assertions.assertEquals(ResponseCode.OPERATION_COMPLETED, finalResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(request.getCorrelationID(), finalResponse.getCorrelationID());
        Assertions.assertEquals(getPillarID(), finalResponse.getFrom());
    }

    @Test
    void checksumPillarGetStatusWrongContributor() {
        addDescription("Tests the GetStatus functionality of the reference pillar for the bad scenario, where a wrong "
                + "contributor id is given.");
        settingsForCUT.getReferenceSettings().getPillarSettings().setAlarmLevel(AlarmLevel.WARNING);
        String wrongContributorId = "wrongContributor";

        addStep("Make and send the request for the actual GetStatus operation",
                "Should be ignored by the pillar.");
        GetStatusRequest request = msgFactory.createGetStatusRequest();
        request.setContributor(wrongContributorId);
        messageBus.sendMessage(request);

        addStep("The pillar should not send an alarm.", "");
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
    }


    public String lookupPillarDestination() {
        IdentifyContributorsForGetStatusRequest identifyRequest = msgFactory.createIdentifyContributorsForGetStatusRequest();
        messageBus.sendMessage(identifyRequest);
        return clientReceiver.waitForMessage(IdentifyContributorsForGetStatusResponse.class).getReplyTo();
    }
}
