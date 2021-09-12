package com.juliuskrah.crdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * @param <E> the element to operate on
 * @author Julius Krah
 */
@Slf4j
public class LwwElementGraph<E extends Comparable<E>> {
    private final Map<Vertex<E>, List<Vertex<E>>> addGraph;
    private final Map<Vertex<E>, List<Vertex<E>>> removeGraph;
    // temporary object to track state
    private final Map<E, VectorClock> elements;
    private final LWWBias bias;
    private VectorClock vectorClock;

    public LwwElementGraph(String nodeId) {
        this(nodeId, LWWBias.ADD);
    }

    /**
     * @param nodeId the node Id
     * @param bias the bias to apply
     */
    public LwwElementGraph(String nodeId, LWWBias bias) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        this.elements = new HashMap<>();
        this.addGraph = new HashMap<>();
        this.removeGraph = new HashMap<>();
        this.vectorClock = VectorClock.of(nodeId);
        this.bias = bias;
    }

    private void prepareAddVertex(E element) {
        this.vectorClock = this.vectorClock.increment();
        this.doAddVertex(element, this.vectorClock);
    }

    private void doAddVertex(E element, VectorClock clock) {
        log.info("Adding: {}...", element);
        var vertex = Vertex.of(element, clock);
        addGraph.computeIfAbsent(vertex, v -> new ArrayList<>());
        updateElements(element, clock);
    }

    private boolean prepareAddEdge(Edge<E> edge) {
        this.vectorClock = this.vectorClock.increment();
        return this.doAddAddEdge(edge);
    }

    private boolean doAddAddEdge(Edge<E> edge) {
        if (!elements.containsKey(edge.getSource().getValue())
                || !elements.containsKey(edge.getDestination().getValue())) {
            // one or both vertices do not exist to create an edge
            return false;
        }
        var vertex1 = edge.getSource();
        var vertex2 = edge.getDestination();
        // add vertex2 to vertex1 adjacency list
        addGraph.computeIfPresent(vertex1, (k, v) -> {
            v.add(vertex2);
            return v;
        });
        // add vertex1 to vertex2 adjacency list
        addGraph.computeIfPresent(vertex2, (k, v) -> {
            v.add(vertex1);
            return v;
        });
        return true;
    }

    private void prepareRemoveVertex(E element) {
        this.vectorClock = this.vectorClock.increment();
        doRemoveVertex(element);
    }

    private void doRemoveVertex(E element) {
        log.info("Removing: {}...", element);
        var vertex = Vertex.of(element, this.vectorClock);
        removeGraph.computeIfAbsent(vertex, v -> new ArrayList<>());
        updateElements(element, this.vectorClock);
    }

    private void prepareRemoveEdge(Edge<E> edge) {
        this.vectorClock = this.vectorClock.increment();
        doRemoveEdge(edge);
    }

    private void doRemoveEdge(Edge<E> edge) {
        var vertex1 = edge.getSource();
        var vertex2 = edge.getDestination();
        // check if edge exist in addGraph
        var neighbors = findAdjacentVertices(vertex1.getValue());
        if (neighbors.contains(vertex2)) {
            // remove vertex2 from vertex1 adjacency list
            addGraph.computeIfPresent(vertex1, (k, v) -> {
                v.remove(vertex2);
                return v;
            });
            // remove vertex1 from vertex2 adjacency list
            addGraph.computeIfPresent(vertex2, (k, v) -> {
                v.remove(vertex1);
                return v;
            });
        } else if (elements.containsKey(edge.getSource().getValue())
                || elements.containsKey(edge.getDestination().getValue())) {
            // remove vertex2 from vertex1 adjacency list
            removeGraph.computeIfPresent(vertex1, (k, v) -> {
                v.remove(vertex2);
                return v;
            });
            // remove vertex1 from vertex2 adjacency list
            removeGraph.computeIfPresent(vertex2, (k, v) -> {
                v.remove(vertex1);
                return v;
            });
        }
    }

    private Stream<Vertex<E>> keys(Map<Vertex<E>, List<Vertex<E>>> map, E value) {
        return map.entrySet().stream().filter(entry -> value.equals(entry.getKey().getValue())).map(Map.Entry::getKey);
    }

    /**
     * Updates the set by checking the elements in addSet against the elements in the removeSet.
     * Keeps any elements that appear in both add and remove sets
     * but have a higher vectorClock in addSet
     * @param element
     * @see #doAdd(Object)
     * @see #doRemove(Object)
     */
    private void updateElements(E element, VectorClock clock) {
        VectorClock removeTime = keys(removeGraph, element).findFirst().map(Vertex::getVectorClock).orElse(null);
        VectorClock addTime = keys(addGraph, element).findFirst().map(Vertex::getVectorClock).orElse(null);
        // element is in both addGraph and removeGraph
        if (removeTime != null && addTime != null) {
            if (removeTime.compareTo(addTime) < 0 //
                    || (removeTime.compareTo(addTime) == 0 && bias == LWWBias.ADD)) {
                elements.put(element, clock);
            } else {
                elements.remove(element);
            }
        } else if (addTime != null) {
            elements.put(element, clock);
        } else {
            elements.remove(element);
        }
    }

    public void addVertex(E element) {
        prepareAddVertex(element);
    }

    public void removeVertex(E element) {
        prepareRemoveVertex(element);
    }

    public boolean addEdge(Edge<E> edge) {
        return prepareAddEdge(edge);
    }

    public void removeEdge(Edge<E> edge) {
        prepareRemoveEdge(edge);
    }

    /**
     * Query for all vertices adjacent to current vertex.
     * @param element current vertex
     * @return all adjacent vertices
     */
    public List<Vertex<E>> findAdjacentVertices(E element) {
        if (Objects.nonNull(elements.get(element))) {
            var vertex = Vertex.of(element, elements.get(element));
            return addGraph.get(vertex);
        }
        return List.of();
    }

    /**
     * @param other the LWW graph to merge with
     * @return merged graph
     */
    public LwwElementGraph<E> merge(LwwElementGraph<E> other) {
        return null;
    }

    public VectorClock findVectorClock(E element) {
        return elements.get(element);
    }

    public int vertexSize() {
        return this.elements.size();
    }

    public boolean containsVertex(E element) {
        var vertex = keys(addGraph, element).findFirst().or(() -> keys(removeGraph, element).findFirst()).orElse(null);
        return vertex != null;
    }

    /**
     * A BIAS value that determines whether to keep add or remove elements.
     * When they share the same vectorClock
     */
    public enum LWWBias {
        /**
         * To ADD.
         */
        ADD,
        /**
         * To REMOVE.
         */
        REMOVE
    }

}
