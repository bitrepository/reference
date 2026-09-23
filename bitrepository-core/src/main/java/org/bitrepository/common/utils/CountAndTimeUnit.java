package org.bitrepository.common.utils;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record CountAndTimeUnit(long count, TimeUnit unit) {
    public CountAndTimeUnit {
        Objects.requireNonNull(unit, "unit");
    }

    /**
     * @deprecated Use {@link #count()} instead
     */
    @Deprecated(forRemoval = true)
    public long getCount() {
        return count;
    }

    /**
     * @deprecated Use {@link #unit()} instead
     */
    @Deprecated(forRemoval = true)
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * <p>Converts this {@code CountAndTimeUnit} to a {@code Duration}.</p>
     *
     * <p>So the opposite conversion of {@link TimeUtils#durationToCountAndTimeUnit(Duration)}
     * except this conversion also handles negative counts.</p>
     *
     * @throws ArithmeticException by overflow of the range of {@code Duration}
     * (roughly +/- 292 277 024 626 years; numerically large counts of minutes, hours and days may overflow).
     */
    public Duration toDuration() {
        return unit.toChronoUnit().getDuration().multipliedBy(count);
    }

}
