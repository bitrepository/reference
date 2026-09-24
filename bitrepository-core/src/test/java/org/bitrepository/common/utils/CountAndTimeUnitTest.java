package org.bitrepository.common.utils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("regressiontest")
class CountAndTimeUnitTest {

    @Test
    void accessorsReturnConstructorValues() {
        CountAndTimeUnit c = new CountAndTimeUnit(42, TimeUnit.SECONDS);
        assertEquals(42, c.count());
        assertEquals(TimeUnit.SECONDS, c.unit());
    }

    @Test
    void compactConstructorRejectsNullUnit() {
        assertThrows(NullPointerException.class, () -> new CountAndTimeUnit(1, null));
    }

    @Test
    void equalityIsComponentBased() {
        CountAndTimeUnit a = new CountAndTimeUnit(5, TimeUnit.MINUTES);
        CountAndTimeUnit b = new CountAndTimeUnit(5, TimeUnit.MINUTES);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void inequalityOnDifferentComponents() {
        CountAndTimeUnit base = new CountAndTimeUnit(5, TimeUnit.MINUTES);
        assertNotEquals(base, new CountAndTimeUnit(6, TimeUnit.MINUTES));
        assertNotEquals(base, new CountAndTimeUnit(5, TimeUnit.SECONDS));
    }
}
