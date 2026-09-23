package org.bitrepository.common.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class CountAndTimeUnitTest {

    @Test
    void convertsToDuration() {
        assertEquals(Duration.ofSeconds(7), new CountAndTimeUnit(7, TimeUnit.SECONDS).toDuration());

        assertEquals(Duration.ofNanos(1), new CountAndTimeUnit(1, TimeUnit.NANOSECONDS).toDuration());
        assertEquals(Duration.ofNanos(3_000), new CountAndTimeUnit(3, TimeUnit.MICROSECONDS).toDuration());
        assertEquals(Duration.ofMillis(7), new CountAndTimeUnit(7, TimeUnit.MILLISECONDS).toDuration());
        assertEquals(Duration.ofSeconds(11), new CountAndTimeUnit(11, TimeUnit.SECONDS).toDuration());
        assertEquals(Duration.ofMinutes(13), new CountAndTimeUnit(13, TimeUnit.MINUTES).toDuration());
        assertEquals(Duration.ofHours(23), new CountAndTimeUnit(23, TimeUnit.HOURS).toDuration());
        assertEquals(Duration.ofDays(31), new CountAndTimeUnit(31, TimeUnit.DAYS).toDuration());

        assertEquals(Duration.ofNanos(-1), new CountAndTimeUnit(-1, TimeUnit.NANOSECONDS).toDuration());
        assertEquals(Duration.ofDays(-31), new CountAndTimeUnit(-31, TimeUnit.DAYS).toDuration());

        // Largest values we can convert correctly
        assertEquals(Duration.ofMinutes(153_722_867_280_912_930L),
                new CountAndTimeUnit(153_722_867_280_912_930L, TimeUnit.MINUTES).toDuration());
        assertEquals(Duration.ofMinutes(-153_722_867_280_912_930L),
                new CountAndTimeUnit(-153_722_867_280_912_930L, TimeUnit.MINUTES).toDuration());
        assertEquals(Duration.ofHours(2_562_047_788_015_215L),
                new CountAndTimeUnit(2_562_047_788_015_215L, TimeUnit.HOURS).toDuration());
        assertEquals(Duration.ofDays(106_751_991_167_300L),
                new CountAndTimeUnit(106_751_991_167_300L, TimeUnit.DAYS).toDuration());
        assertEquals(Duration.ofDays(-106_751_991_167_300L),
                new CountAndTimeUnit(-106_751_991_167_300L, TimeUnit.DAYS).toDuration());
    }

    @Test
    void failsOnOverflow() {
        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(153_722_867_280_912_931L, TimeUnit.MINUTES).toDuration());
        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(-153_722_867_280_912_931L, TimeUnit.MINUTES).toDuration());
        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(2_562_047_788_015_216L, TimeUnit.HOURS).toDuration());
        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(106_751_991_167_301L, TimeUnit.DAYS).toDuration());
        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(-106_751_991_167_301L, TimeUnit.DAYS).toDuration());

        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.DAYS).toDuration());
        assertThrows(ArithmeticException.class,
                () -> new CountAndTimeUnit(Long.MIN_VALUE, TimeUnit.DAYS).toDuration());
    }

}
