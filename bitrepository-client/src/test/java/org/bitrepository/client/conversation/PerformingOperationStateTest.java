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
package org.bitrepository.client.conversation;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformingOperationStateTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void expectedContributorsAreTheIDsOfTheSelectedComponentsTest() {
        StubState state = new StubState(List.of(
                new SelectedComponentInfo("pillar1", "topic1"),
                new SelectedComponentInfo("pillar2", "topic2")));

        assertEquals(Set.of("pillar1", "pillar2"), Set.copyOf(state.outstandingComponents()));
        assertEquals(2, state.outstandingComponents().size());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void aComponentSelectedTwiceIsOnlyExpectedOnceTest() {
        StubState state = new StubState(List.of(
                new SelectedComponentInfo("pillar1", "topic1"),
                new SelectedComponentInfo("pillar1", "otherTopic")));

        assertEquals(List.of("pillar1"), List.copyOf(state.outstandingComponents()));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void noSelectedComponentsMeansNoExpectedContributorsTest() {
        assertEquals(0, new StubState(List.of()).outstandingComponents().size());
    }

    /** Implements only what the constructor-time behaviour under test does not need. */
    private static class StubState extends PerformingOperationState {
        StubState(Collection<SelectedComponentInfo> expectedContributors) {
            super(expectedContributors);
        }

        Collection<String> outstandingComponents() {
            return getOutstandingComponents();
        }

        @Override
        protected void generateContributorCompleteEvent(MessageResponse msg) {
        }

        @Override
        protected void sendRequest() {
        }

        @Override
        protected ConversationContext getContext() {
            return null;
        }

        @Override
        protected String getPrimitiveName() {
            return "StubPrimitive";
        }
    }
}
