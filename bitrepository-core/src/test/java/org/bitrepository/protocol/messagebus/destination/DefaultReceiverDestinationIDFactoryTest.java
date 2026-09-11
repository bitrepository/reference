/*
 * #%L
 * Bitmagasin integrationstest
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.messagebus.destination;

import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class DefaultReceiverDestinationIDFactoryTest {

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void receiverDestinationIsAQueueEvenWhenCollectionDestinationIsATopic() {
        DefaultReceiverDestinationIDFactory factory = new DefaultReceiverDestinationIDFactory();

        String receiverDestinationID =
                factory.getReceiverDestinationID("SomePillar", "topic://dummy-repository-id");

        Assertions.assertEquals("queue://dummy-repository-id-SomePillar", receiverDestinationID);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void receiverDestinationIsAQueueWhenCollectionDestinationHasNoScheme() {
        DefaultReceiverDestinationIDFactory factory = new DefaultReceiverDestinationIDFactory();

        String receiverDestinationID =
                factory.getReceiverDestinationID("SomePillar", "dummy-repository-id");

        Assertions.assertEquals("queue://dummy-repository-id-SomePillar", receiverDestinationID);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void userSpecificFactorySuffixesTheQueueDestinationWithTheUsername() {
        UserSpecificReceiverDestinationIDFactory factory = new UserSpecificReceiverDestinationIDFactory();

        String receiverDestinationID =
                factory.getReceiverDestinationID("SomePillar", "topic://dummy-repository-id");

        Assertions.assertEquals(
                "queue://dummy-repository-id-SomePillar-" + System.getProperty("user.name"),
                receiverDestinationID);
    }
}
