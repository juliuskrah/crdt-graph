package com.juliuskrah.crdt;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A vector clock used for tracking the time an object was created.
 * @author Julius Krah
 */
public final class VectorClock implements Comparable<VectorClock> {
    private final String key;
    private final Map<String, Long> entries;

    private VectorClock(String key, Map<String, Long> entries) {
        this.key = key;
        this.entries = entries;
    }

    private VectorClock(String key) {
        this(key, Map.of());
    }

    public static VectorClock of(String key) {
        return new VectorClock(key);
    }

    public static VectorClock of(String key, Map<String, Long> entries) {
        return new VectorClock(key, entries);
    }

    /**
     * increments the logical clock in the vector by 1.
     * @return incremented VectorClock
     */
    public VectorClock increment() {
        final long counter = entries.getOrDefault(key, 1L) + 1L;
        // The internal state of the map must remain unchanged
        return new VectorClock(key, Collections.unmodifiableMap(Map.of(key, counter)));
    }

    /**
     * Computes the difference between {@linkplain this} clock and {@linkplain other} clock.
     * @param other another Vector clock to {@link #compareTo(VectorClock)}
     * @return all differences
     */
    private Set<Long> calculateDiffs(VectorClock other) {
        Set<String> allKeys = new HashSet<>(entries.keySet());
        allKeys.addAll(other.entries.keySet());
        return allKeys.stream().map(existingKey ->
            entries.getOrDefault(existingKey, 0L) - other.entries.getOrDefault(key, 0L)
        ).collect(toSet());
    }

    /**
     * Merges this vector clock with other.
     * @param other another clock
     * @return merged clocks
     */
    public VectorClock merge(VectorClock other) {
        Map<String, Long> merged = Stream.concat(entries.entrySet().stream(), other.entries.entrySet().stream())
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
        return new VectorClock(this.key, merged);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(VectorClock other) {
        final Set<Long> diffs = calculateDiffs(other);
        final boolean isGreater = diffs.stream().anyMatch(diff -> diff > 0);
        final boolean isLess = diffs.stream().anyMatch(diff -> diff < 0);
        if (isGreater && isLess) {
            return this.key.compareTo(other.key);
        }
        if (isLess) {
            return -1;
        }
        return isGreater ? 1 : key.compareTo(other.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toString(entries, "entries");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VectorClock that = (VectorClock) o;
        return calculateDiffs(that).stream().allMatch(diff -> diff == 0);
    }
}
