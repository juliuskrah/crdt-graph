package com.juliuskrah.crdt;

/**
 * @param <E> the element to operate on
 * @author Julius Krah
 */
public class Edge<E extends Comparable<E>> {
    private final Vertex<E> source;
    private final Vertex<E> destination;

    private Edge(Vertex<E> source, Vertex<E> destination) {
        this.source = source;
        this.destination = destination;
    }

    public static <E extends Comparable<E>> Edge<E> of(Vertex<E> source, Vertex<E> destination) {
        return new Edge<>(source, destination);
    }

    public Vertex<E> getSource() {
        return this.source;
    }

    public Vertex<E> getDestination() {
        return this.destination;
    }
}
