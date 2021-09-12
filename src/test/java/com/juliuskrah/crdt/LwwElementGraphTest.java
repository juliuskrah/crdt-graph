package com.juliuskrah.crdt;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.juliuskrah.crdt.LwwElementGraph.LWWBias;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
class LwwElementGraphTest {

    @BeforeEach
    private void init() {
        // generally repeated operations are here.
        // however to make the tests clearly demonstrate what is being added and removed,
        // I am going to make each test verbose
    }

    @Test
    @DisplayName("add a vertex/edge")
    void testAddVertexAndEdge() {
        final var elementGraph = new LwwElementGraph<String>("node 1");
        // -- add vertex
        elementGraph.addVertex("julius");
        elementGraph.addVertex("james");
        elementGraph.addVertex("zumar");
        elementGraph.addVertex("alice");
        elementGraph.addVertex("freda");
        final int expected = 5;
        assertEquals(expected, elementGraph.vertexSize());

        // -- add edge
        var vectorClockJulius = elementGraph.findVectorClock("julius") != null
            ? elementGraph.findVectorClock("julius")
            : VectorClock.of("node 1");
        var vectorClockJames = elementGraph.findVectorClock("james") != null
            ? elementGraph.findVectorClock("james")
            : VectorClock.of("node 1");
        // Julius and James exist as vertices - edge is possible
        var edge = Edge.of(
            Vertex.of("julius", vectorClockJulius),
            Vertex.of("james", vectorClockJames)
        );
        var isSuccess = elementGraph.addEdge(edge);
        assertTrue(isSuccess);

        var vectorClockKwame = elementGraph.findVectorClock("kwame") != null
            ? elementGraph.findVectorClock("kwame")
            : VectorClock.of("node 1");
        // James exist as a vertex but not Kwame - edge is not possible
        edge = Edge.of(
            Vertex.of("kwame", vectorClockKwame),
            Vertex.of("james", vectorClockJames)
        );
        isSuccess = elementGraph.addEdge(edge);
        assertFalse(isSuccess);
    }

    @Test
    @DisplayName("remove a vertex/edge")
    void testRemoveVertexAndEdge() {
        final var elementGraph = new LwwElementGraph<String>("node 1", LWWBias.REMOVE);
        // -- remove vertex
        elementGraph.addVertex("julius");
        elementGraph.addVertex("james");
        elementGraph.removeVertex("james");
        assertTrue(elementGraph.containsVertex("james")); // contains added vertex
        assertTrue(elementGraph.containsVertex("julius")); // contains removed vertex
        assertEquals(1, elementGraph.vertexSize()); // bias favours remove
        elementGraph.addVertex("james"); // allows re-insertion
        assertEquals(2, elementGraph.vertexSize());

        // -- remove edge
        elementGraph.addVertex("james");
        var vectorClockJulius = elementGraph.findVectorClock("julius") != null
            ? elementGraph.findVectorClock("julius")
            : VectorClock.of("node 1");
        var vectorClockJames = elementGraph.findVectorClock("james") != null
            ? elementGraph.findVectorClock("james")
            : VectorClock.of("node 1");
        // Julius and James exist as vertices - edge is possible
        var edge = Edge.of(
            Vertex.of("julius", vectorClockJulius),
            Vertex.of("james", vectorClockJames)
        );
        elementGraph.addEdge(edge);
        assertIterableEquals(
            List.of("james"),
            elementGraph.findAdjacentVertices("julius")
                .stream().map(Vertex::getValue).collect(toList())
        );
        // after removing edge, there are no vertices left
        elementGraph.removeEdge(edge);
        assertIterableEquals(
            List.of(),
            elementGraph.findAdjacentVertices("julius")
                .stream().map(Vertex::getValue).collect(toList())
        );
    }

    @Test
    @DisplayName("check if a vertex is in the graph")
    void testCheckVertexInGraph() {
        final var elementGraph = new LwwElementGraph<String>("node 1");
        // -- check vertex in graph
        elementGraph.addVertex("julius");
        assertTrue(elementGraph.containsVertex("julius"));
        assertFalse(elementGraph.containsVertex("james"));
    }

    @Test
    @DisplayName("query for all vertices connected to a vertex")
    void testQueryVertices() {
        final var elementGraph = new LwwElementGraph<String>("node 1");
        // -- query all connected vertices
        elementGraph.addVertex("julius");
        elementGraph.addVertex("james");
        elementGraph.addVertex("zumar");
        elementGraph.addVertex("alice");
        elementGraph.addVertex("freda");

        var juliusAliceEdge = Edge.of(
            Vertex.of("julius", elementGraph.findVectorClock("julius")),
            Vertex.of("alice", elementGraph.findVectorClock("alice"))
        );
        var juliusJamesEdge = Edge.of(
            Vertex.of("julius", elementGraph.findVectorClock("julius")),
            Vertex.of("james", elementGraph.findVectorClock("james"))
        );
        elementGraph.addEdge(juliusAliceEdge);
        elementGraph.addEdge(juliusJamesEdge);

        var verticesOfJulius = elementGraph.findAdjacentVertices("julius");
        assertIterableEquals(
            List.of("alice", "james"),
            verticesOfJulius.stream().map(Vertex::getValue).collect(toList())
        );
    }

    @Test
    @DisplayName("find any path between two vertices")
    void findPathBetweenVertices() {

    }

    @Test
    @DisplayName("merge with concurrent changes from other graph/replica")
    void testMergeTwoReplicas() {

    }
}
