package com.juliuskrah.crdt;

import java.util.Objects;

/**
 * @param <E> the element to operate on
 * @author Julius Krah
 */
public final class Vertex<E extends Comparable<E>> {
    private final E value;
    private final VectorClock vectorClock;

    private Vertex(E value, VectorClock clock) {
        this.value = value;
        this.vectorClock = clock;
    }

    public static <E extends Comparable<E>> Vertex<E> of(E value, VectorClock clock) {
        return new Vertex<>(value, clock);
    }

    public E getValue() {
        return this.value;
    }

    public VectorClock getVectorClock() {
        return this.vectorClock;
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
        var vertex = (Vertex<?>) o;

        return Objects.equals(vectorClock, vertex.vectorClock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return vectorClock.hashCode();
    }
}
