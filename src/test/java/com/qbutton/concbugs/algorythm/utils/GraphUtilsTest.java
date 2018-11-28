package com.qbutton.concbugs.algorythm.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GraphUtils class")
class GraphUtilsTest {

    @Nested
    @DisplayName("removes object from graph")
    class RemoveObject {

        @Test
        @DisplayName("correctly when data is valid")
        void removeObject_success() {
            //given

            /*
                ho1 -> ho2
                ho1 -> ho3
                ho3 -> ho2
                ho3 -> ho1
            */
            ProgramPoint point1 = new ProgramPoint("a", 1);
            HeapObject ho1 = new HeapObject(point1, Integer.class);

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, String.class);

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, Object.class);

            Map<HeapObject, Set<HeapObject>> graphMap = ImmutableMap.of(
                    ho1, ImmutableSet.of(ho2, ho3),
                    ho3, ImmutableSet.of(ho2, ho1)
            );

            Graph initialGraph = new Graph(graphMap);

            //when
            Graph graph = GraphUtils.removeObject(initialGraph, ho1);

            //then
            //ho3 -> ho2 is the only remaining edge
            Map<HeapObject, Set<HeapObject>> neighbors = graph.getNeighbors();
            assertThat(neighbors.size(), is(1));
            assertTrue(neighbors.containsKey(ho3));
            assertThat(neighbors.get(ho3).size(), is(1));
            assertThat(neighbors.get(ho3).iterator().next(), is(ho2));
        }

        @Test
        @DisplayName("with exception when object to remove is not in graph")
        void removeObject_failure_objectNotInGraph() {
            //given
            Graph initialGraph = new Graph(ImmutableMap.of());

            //when
            //then
            assertThrows(AlgorithmValidationException.class,
                    () -> GraphUtils.removeObject(initialGraph,
                            new HeapObject(
                                    new ProgramPoint("any", 5),
                                    Integer.class
                            )));
        }
    }

    @Nested
    @DisplayName("replaces object in graph")
    class ReplaceObject {

        @DisplayName("correctly when data is valid")
        @Test
        void replaceObject_success() {
            //given

            /*
                ho1 -> ho2
                ho1 -> ho3
                ho3 -> ho2
                ho3 -> ho1
            */
            ProgramPoint point1 = new ProgramPoint("a", 1);
            HeapObject ho1 = new HeapObject(point1, Integer.class);

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, String.class);

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, Object.class);

            ProgramPoint point4 = new ProgramPoint("d", 4);
            HeapObject ho4 = new HeapObject(point4, Object.class);

            Map<HeapObject, Set<HeapObject>> graphMap = ImmutableMap.of(
                    ho1, ImmutableSet.of(ho2, ho3),
                    ho3, ImmutableSet.of(ho2, ho1)
            );

            Graph initialGraph = new Graph(graphMap);
            Set<HeapObject> roots = ImmutableSet.of(ho1, ho2);

            //when
            GraphUtils.ReplaceNodeResult replaceNodeResult = GraphUtils.replaceNode(initialGraph, roots, ho1, ho4);

            //then
            /*
                ho4 -> ho2
                ho4 -> ho3
                ho3 -> ho2
                ho3 -> ho4
            */
            Map<HeapObject, Set<HeapObject>> neighbors = replaceNodeResult.getGraph().getNeighbors();
            assertThat(neighbors.size(), is(2));
            assertTrue(neighbors.containsKey(ho3));
            assertTrue(neighbors.containsKey(ho4));
            assertThat(neighbors.get(ho3).size(), is(2));
            assertTrue(neighbors.get(ho3).contains(ho2));
            assertTrue(neighbors.get(ho3).contains(ho4));
            assertThat(neighbors.get(ho4).size(), is(2));
            assertTrue(neighbors.get(ho4).contains(ho2));
            assertTrue(neighbors.get(ho4).contains(ho3));

            Set<HeapObject> updatedRoots = replaceNodeResult.getRoots();
            assertThat(updatedRoots.size(), is(2));
            assertTrue(updatedRoots.contains(ho2));
            assertTrue(updatedRoots.contains(ho4));
        }

        @DisplayName("without adding new one when new heapObject is null")
        @Test
        void replaceObject_success_onlyRemoval() {
            //given

            /*
                ho1 -> ho2
                ho1 -> ho3
                ho3 -> ho2
                ho3 -> ho1
            */
            ProgramPoint point1 = new ProgramPoint("a", 1);
            HeapObject ho1 = new HeapObject(point1, Integer.class);

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, String.class);

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, Object.class);

            Map<HeapObject, Set<HeapObject>> graphMap = ImmutableMap.of(
                    ho1, ImmutableSet.of(ho2, ho3),
                    ho3, ImmutableSet.of(ho2, ho1)
            );

            Graph initialGraph = new Graph(graphMap);
            Set<HeapObject> roots = ImmutableSet.of(ho1, ho2);

            //when
            GraphUtils.ReplaceNodeResult replaceNodeResult = GraphUtils.replaceNode(initialGraph, roots, ho1, null);

            //then
            //ho3 -> ho2 is the only remaining edge
            Map<HeapObject, Set<HeapObject>> neighbors = replaceNodeResult.getGraph().getNeighbors();
            assertThat(neighbors.size(), is(1));
            assertTrue(neighbors.containsKey(ho3));
            assertThat(neighbors.get(ho3).size(), is(1));
            assertThat(neighbors.get(ho3).iterator().next(), is(ho2));

            Set<HeapObject> updatedRoots = replaceNodeResult.getRoots();
            assertThat(updatedRoots.size(), is(1));
            assertTrue(updatedRoots.contains(ho2));
        }

        @Test
        @DisplayName("with exception when object to replace is not in graph")
        void replaceObject_failure_objectNotInGraph() {
            //given
            Graph initialGraph = new Graph(ImmutableMap.of());

            //when
            //then
            assertThrows(AlgorithmValidationException.class,
                    () -> GraphUtils.replaceNode(
                            initialGraph,
                            emptySet(),
                            new HeapObject(
                                    new ProgramPoint("any", 5),
                                    Integer.class
                            ),
                            null));
        }
    }
}