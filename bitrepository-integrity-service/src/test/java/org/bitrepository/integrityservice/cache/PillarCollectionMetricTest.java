package org.bitrepository.integrityservice.cache;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@Tag("regressiontest")
class PillarCollectionMetricTest {

    @Test
    void canonicalConstructorStoresValues() {
        Instant ts = Instant.ofEpochMilli(1000);
        PillarCollectionMetric m = new PillarCollectionMetric(10L, 5L, ts);
        assertEquals(10L, m.pillarCollectionSize());
        assertEquals(5L, m.pillarFileCount());
        assertEquals(ts, m.oldestChecksumTimestamp());
    }

    @Test
    void nullableConstructorCoercesNullSizeToZero() {
        PillarCollectionMetric m = new PillarCollectionMetric((Long) null, (Long) null, null);
        assertEquals(0L, m.pillarCollectionSize());
        assertEquals(0L, m.pillarFileCount());
        assertNull(m.oldestChecksumTimestamp());
    }

    @Test
    void equalityIsComponentBased() {
        Instant ts = Instant.ofEpochMilli(1000);
        PillarCollectionMetric a = new PillarCollectionMetric(10L, 5L, ts);
        PillarCollectionMetric b = new PillarCollectionMetric(10L, 5L, ts);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
