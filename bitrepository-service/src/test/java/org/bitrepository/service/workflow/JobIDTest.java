package org.bitrepository.service.workflow;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("regressiontest")
class JobIDTest {

    @Test
    void accessorsReturnConstructorValues() {
        JobID id = new JobID("MyWorkflow", "col1");
        assertEquals("MyWorkflow", id.workflowName());
        assertEquals("col1", id.collectionID());
    }

    @Test
    void equalityIsComponentBased() {
        JobID a = new JobID("wf", "col");
        JobID b = new JobID("wf", "col");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void inequalityOnDifferentComponents() {
        JobID base = new JobID("wf", "col");
        assertNotEquals(base, new JobID("other", "col"));
        assertNotEquals(base, new JobID("wf", "other"));
    }

    @Test
    void toStringUsesCustomFormat() {
        assertEquals("wf-col", new JobID("wf", "col").toString());
    }

    @Test
    void nullComponentsAreAllowed() {
        // Records do not reject nulls unless the compact constructor does so;
        // JobID has no validation, so nulls must be accepted.
        JobID id = new JobID(null, null);
        assertNull(id.workflowName());
        assertNull(id.collectionID());
        assertEquals(new JobID(null, null), id);
    }
}
