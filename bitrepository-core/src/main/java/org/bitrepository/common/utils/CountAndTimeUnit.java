package org.bitrepository.common.utils;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CountAndTimeUnit {
    private final long count;
    private final TimeUnit unit;

    public CountAndTimeUnit(long count, TimeUnit unit) {
        this.count = count;
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    public long getCount() {
        return count;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountAndTimeUnit that = (CountAndTimeUnit) o;
        return count == that.count && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, unit);
    }

}
