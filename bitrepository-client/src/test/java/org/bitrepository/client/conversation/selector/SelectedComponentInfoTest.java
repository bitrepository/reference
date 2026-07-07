package org.bitrepository.client.conversation.selector;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("regressiontest")
class SelectedComponentInfoTest {

    @Test
    void delegationMethodsReturnCorrectComponents() {
        SelectedComponentInfo info = new SelectedComponentInfo("pillar1", "pillar1-topic");
        assertEquals("pillar1", info.getID());
        assertEquals("pillar1-topic", info.getDestination());
    }

    @Test
    void equalityIsComponentBased() {
        SelectedComponentInfo a = new SelectedComponentInfo("p1", "t1");
        SelectedComponentInfo b = new SelectedComponentInfo("p1", "t1");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsComponentValues() {
        String s = new SelectedComponentInfo("p1", "t1").toString();
        assertTrue(s.contains("p1"));
        assertTrue(s.contains("t1"));
    }
}
