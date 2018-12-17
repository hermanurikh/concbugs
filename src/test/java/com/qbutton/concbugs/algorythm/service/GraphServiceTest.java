package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphService class")
class GraphServiceTest {

    @Mock
    private ReflectionService reflectionService;

    private GraphService graphService;

    @BeforeEach
    void init() {
        graphService = new GraphService(reflectionService);
    }

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
                ho2 -> 0
            */
            ProgramPoint point1 = new ProgramPoint("a", 1);
            HeapObject ho1 = new HeapObject(point1, Integer.class);

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, String.class);

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, Object.class);

            Map<HeapObject, Set<HeapObject>> graphMap = ImmutableMap.of(
                    ho1, ImmutableSet.of(ho2, ho3),
                    ho3, ImmutableSet.of(ho2, ho1),
                    ho2, Collections.emptySet()
            );

            Graph initialGraph = new Graph(graphMap);

            //when
            Graph graph = graphService.removeObject(initialGraph, ho1);

            //then
            /*
                ho3 -> ho2
                ho2 -> 0
            */
            Map<HeapObject, Set<HeapObject>> neighbors = graph.getNeighbors();
            assertThat(neighbors.size(), is(2));
            assertTrue(neighbors.containsKey(ho3));
            assertThat(neighbors.get(ho3).size(), is(1));
            assertThat(neighbors.get(ho3).iterator().next(), is(ho2));
            assertTrue(neighbors.containsKey(ho2));
            assertThat(neighbors.get(ho2).size(), is(0));
        }

        @Test
        @DisplayName("with exception when object to remove is not in graph")
        void removeObject_failure_objectNotInGraph() {
            //given
            Graph initialGraph = new Graph(ImmutableMap.of());

            //when
            //then
            assertThrows(AlgorithmValidationException.class,
                    () -> graphService.removeObject(initialGraph,
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
                ho2 -> 0
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
                    ho3, ImmutableSet.of(ho2, ho1),
                    ho2, Collections.emptySet()
            );

            Graph initialGraph = new Graph(graphMap);
            Set<HeapObject> roots = ImmutableSet.of(ho1, ho2);

            //when
            GraphService.ReplaceNodeResult replaceNodeResult = graphService.replaceNode(initialGraph, roots, ho1, ho4);

            //then
            /*
                ho4 -> ho2
                ho4 -> ho3
                ho3 -> ho2
                ho3 -> ho4
                ho2 -> 0
            */
            Map<HeapObject, Set<HeapObject>> neighbors = replaceNodeResult.getGraph().getNeighbors();
            assertThat(neighbors.size(), is(3));
            assertTrue(neighbors.containsKey(ho3));
            assertTrue(neighbors.containsKey(ho4));
            assertTrue(neighbors.containsKey(ho2));
            assertThat(neighbors.get(ho3).size(), is(2));
            assertTrue(neighbors.get(ho3).contains(ho2));
            assertTrue(neighbors.get(ho3).contains(ho4));
            assertThat(neighbors.get(ho4).size(), is(2));
            assertTrue(neighbors.get(ho4).contains(ho2));
            assertTrue(neighbors.get(ho4).contains(ho3));
            assertThat(neighbors.get(ho2).size(), is(0));

            Set<HeapObject> updatedRoots = replaceNodeResult.getRoots();
            assertThat(updatedRoots.size(), is(2));
            assertTrue(updatedRoots.contains(ho2));
            assertTrue(updatedRoots.contains(ho4));
        }

        @DisplayName("correctly without self link")
        @Test
        void replaceObject_success_selfLinkIsAbsent() {
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
            GraphService.ReplaceNodeResult replaceNodeResult = graphService.replaceNode(initialGraph, roots, ho3, ho1);

            //then
            /*
                ho1 -> ho2
            */
            Map<HeapObject, Set<HeapObject>> neighbors = replaceNodeResult.getGraph().getNeighbors();
            assertThat(neighbors.size(), is(1));
            assertTrue(neighbors.containsKey(ho1));
            assertThat(neighbors.get(ho1).size(), is(1));
            assertTrue(neighbors.get(ho1).contains(ho2));

            Set<HeapObject> updatedRoots = replaceNodeResult.getRoots();
            assertThat(updatedRoots.size(), is(2));
            assertTrue(updatedRoots.contains(ho1));
            assertTrue(updatedRoots.contains(ho2));
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
            GraphService.ReplaceNodeResult replaceNodeResult = graphService.replaceNode(initialGraph, roots, ho1, null);

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
                    () -> graphService.replaceNode(
                            initialGraph,
                            emptySet(),
                            new HeapObject(
                                    new ProgramPoint("any", 5),
                                    Integer.class
                            ),
                            null));
        }
    }

    @Nested
    @DisplayName("slices out node in a graph")
    class SliceOutNode {
        @DisplayName("correctly when data is valid")
        @Test
        void sliceOutNode_success() {
            //given

            /*
                ho1 -> ho2
                ho2 -> ho1
                ho2 -> ho3
                ho3 -> ho1
                ho4 -> ho2
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
                    ho1, ImmutableSet.of(ho2),
                    ho2, ImmutableSet.of(ho1, ho3),
                    ho3, ImmutableSet.of(ho1),
                    ho4, ImmutableSet.of(ho2)
            );

            Graph initialGraph = new Graph(graphMap);
            Set<HeapObject> roots = ImmutableSet.of(ho2);

            //when
            GraphService.ReplaceNodeResult replaceNodeResult = graphService.spliceOutNode(initialGraph, roots, ho2);

            //then
            /*
                ho1 -> ho3
                ho4 -> ho1
                ho4 -> ho3
                ho3 -> ho1
            */
            Map<HeapObject, Set<HeapObject>> neighbors = replaceNodeResult.getGraph().getNeighbors();
            assertThat(neighbors.size(), is(3));
            assertTrue(neighbors.containsKey(ho1));
            assertThat(neighbors.get(ho1).size(), is(1));
            assertThat(neighbors.get(ho1).iterator().next(), is(ho3));
            assertTrue(neighbors.containsKey(ho3));
            assertThat(neighbors.get(ho3).size(), is(1));
            assertThat(neighbors.get(ho3).iterator().next(), is(ho1));
            assertTrue(neighbors.containsKey(ho4));
            assertThat(neighbors.get(ho4).size(), is(2));
            assertTrue(neighbors.get(ho4).contains(ho1));
            assertTrue(neighbors.get(ho4).contains(ho3));

            Set<HeapObject> updatedRoots = replaceNodeResult.getRoots();
            assertThat(updatedRoots.size(), is(2));
            assertTrue(updatedRoots.contains(ho1));
            assertTrue(updatedRoots.contains(ho3));
        }

        @DisplayName("correctly without self link")
        @Test
        void sliceOutNode_success_selfLinkIsAbsent() {
            //given

            /*
                ho1 -> ho2
                ho2 -> ho3
                ho2 -> ho1
            */
            ProgramPoint point1 = new ProgramPoint("a", 1);
            HeapObject ho1 = new HeapObject(point1, Integer.class);

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, String.class);

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, Object.class);

            Map<HeapObject, Set<HeapObject>> graphMap = ImmutableMap.of(
                    ho1, ImmutableSet.of(ho2),
                    ho2, ImmutableSet.of(ho1, ho3)
            );

            Graph initialGraph = new Graph(graphMap);
            Set<HeapObject> roots = ImmutableSet.of(ho2);

            //when
            GraphService.ReplaceNodeResult replaceNodeResult = graphService.spliceOutNode(initialGraph, roots, ho2);

            //then
            /*
                ho1 -> ho3
            */
            Map<HeapObject, Set<HeapObject>> neighbors = replaceNodeResult.getGraph().getNeighbors();
            assertThat(neighbors.size(), is(1));
            assertTrue(neighbors.containsKey(ho1));
            assertThat(neighbors.get(ho1).size(), is(1));
            assertThat(neighbors.get(ho1).iterator().next(), is(ho3));

            Set<HeapObject> updatedRoots = replaceNodeResult.getRoots();
            assertThat(updatedRoots.size(), is(2));
            assertTrue(updatedRoots.contains(ho1));
            assertTrue(updatedRoots.contains(ho3));
        }

        @DisplayName("with exception if node object is not in graph")
        @Test
        void sliceOutNode_failure_nodeNotFound() {
            //given
            Graph graph = new Graph(Collections.emptyMap());
            assertThrows(AlgorithmValidationException.class,
                    () -> graphService.spliceOutNode(
                            graph,
                            Collections.emptySet(),
                            new HeapObject(
                                    new ProgramPoint("1", 1), Integer.class
                            )));
        }
    }

    @Nested
    @DisplayName("post processes methods")
    class PostProcess {

        @Test
        @DisplayName("correctly")
        void postProcess() {
            //given

            HeapObject ho1 = new HeapObject(new ProgramPoint("v1", 32), Statement.class);
            HeapObject ho2 = new HeapObject(new ProgramPoint("v2", 33), String.class);
            HeapObject ho3 = new HeapObject(new ProgramPoint("v3", 34), Integer.class);
            HeapObject ho4 = new HeapObject(new ProgramPoint("v4", 35), Statement.class);
            HeapObject ho5 = new HeapObject(new ProgramPoint("v5", 36), Double.class);
            HeapObject ho6 = new HeapObject(new ProgramPoint("v6", 37), Long.class);

            when(reflectionService.getSubclassesOf(any())).thenAnswer(invocation -> {
                Class<?> clazz = (Class<?>) invocation.getArguments()[0];

                if (clazz.equals(Statement.class)) {
                    return ImmutableSet.of(
                            Statement.class,
                            BranchStatement.class,
                            CrossAssignmentStatement.class,
                            DeclarationStatement.class,
                            InnerAssignmentStatement.class,
                            MethodStatement.class,
                            SequentialStatement.class,
                            SynchronizedStatement.class,
                            WaitStatement.class
                    );
                }

                if (clazz.equals(String.class)) {
                    return ImmutableSet.of(String.class);
                }

                if (clazz.equals(Integer.class)) {
                    return ImmutableSet.of(Integer.class);
                }

                if (clazz.equals(Double.class)) {
                    return ImmutableSet.of(Double.class);
                }

                if (clazz.equals(Long.class)) {
                    return ImmutableSet.of(Long.class);
                }

                return null;
            });

            /*
            ho1 -> ho2
            ho1 -> ho3
            ho2 -> ho3
             */
            State state1 = new State(
                    new Graph(
                            ImmutableMap.of(
                                    ho1, ImmutableSet.of(ho2, ho3),
                                    ho2, ImmutableSet.of(ho3),
                                    ho3, emptySet())
                    ),

                    emptySet(),
                    emptyList(),
                    emptyList(),
                    emptySet()
            );

            /*
            ho4 -> ho5
            ho4 -> ho6
            ho5 -> ho4
             */
            State state2 = new State(
                    new Graph(
                            ImmutableMap.of(
                                    ho4, ImmutableSet.of(ho5, ho6),
                                    ho5, ImmutableSet.of(ho4),
                                    ho6, emptySet())
                    ),

                    emptySet(),
                    emptyList(),
                    emptyList(),
                    emptySet()
            );


            //when
            Graph resultGraph = graphService.postProcess(ImmutableList.of(state1, state2));

            //then
            /*
            Statement has 9 subclasses, including itself
                - for each of 9 subclasses, expect a link to ho2, ho3, ho5 and ho6 with generic type
                - a link from ho2 to ho3
                - a link from ho5 to each of 9 subclasses
             */

            HeapObject expectedStatementHo1 = new HeapObject(ProgramPoint.UNKNOWN, Statement.class);
            HeapObject expectedStatementHo2 = new HeapObject(ProgramPoint.UNKNOWN, BranchStatement.class);
            HeapObject expectedStatementHo3 = new HeapObject(ProgramPoint.UNKNOWN, CrossAssignmentStatement.class);
            HeapObject expectedStatementHo4 = new HeapObject(ProgramPoint.UNKNOWN, DeclarationStatement.class);
            HeapObject expectedStatementHo5 = new HeapObject(ProgramPoint.UNKNOWN, InnerAssignmentStatement.class);
            HeapObject expectedStatementHo6 = new HeapObject(ProgramPoint.UNKNOWN, MethodStatement.class);
            HeapObject expectedStatementHo7 = new HeapObject(ProgramPoint.UNKNOWN, SequentialStatement.class);
            HeapObject expectedStatementHo8 = new HeapObject(ProgramPoint.UNKNOWN, SynchronizedStatement.class);
            HeapObject expectedStatementHo9 = new HeapObject(ProgramPoint.UNKNOWN, WaitStatement.class);
            HeapObject expectedHo2 = new HeapObject(ProgramPoint.UNKNOWN, String.class);
            HeapObject expectedHo3 = new HeapObject(ProgramPoint.UNKNOWN, Integer.class);
            HeapObject expectedHo5 = new HeapObject(ProgramPoint.UNKNOWN, Double.class);
            HeapObject expectedHo6 = new HeapObject(ProgramPoint.UNKNOWN, Long.class);
            ImmutableSet<HeapObject> statementEdges = ImmutableSet.of(expectedHo2, expectedHo3, expectedHo5, expectedHo6);

            Map<HeapObject, Set<HeapObject>> graphMap = resultGraph.getNeighbors();
            assertThat(graphMap.size(), is(13));

            assertTrue(graphMap.containsKey(expectedStatementHo1));
            assertThat(graphMap.get(expectedStatementHo1).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo1).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo2));
            assertThat(graphMap.get(expectedStatementHo2).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo2).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo3));
            assertThat(graphMap.get(expectedStatementHo3).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo3).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo4));
            assertThat(graphMap.get(expectedStatementHo4).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo4).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo5));
            assertThat(graphMap.get(expectedStatementHo5).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo5).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo6));
            assertThat(graphMap.get(expectedStatementHo6).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo6).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo7));
            assertThat(graphMap.get(expectedStatementHo7).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo7).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo8));
            assertThat(graphMap.get(expectedStatementHo8).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo8).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedStatementHo9));
            assertThat(graphMap.get(expectedStatementHo9).size(), is(4));
            assertTrue(graphMap.get(expectedStatementHo9).containsAll(statementEdges));

            assertTrue(graphMap.containsKey(expectedHo2));
            assertThat(graphMap.get(expectedHo2).size(), is(1));
            assertThat(graphMap.get(expectedHo2).iterator().next(), is(expectedHo3));

            assertTrue(graphMap.containsKey(expectedHo3));
            assertTrue(graphMap.get(expectedHo3).isEmpty());

            assertTrue(graphMap.containsKey(expectedHo5));
            assertThat(graphMap.get(expectedHo5).size(), is(9));
            assertTrue(graphMap.get(expectedHo5).containsAll(
                    ImmutableSet.of(
                            expectedStatementHo1,
                            expectedStatementHo2,
                            expectedStatementHo3,
                            expectedStatementHo4,
                            expectedStatementHo5,
                            expectedStatementHo6,
                            expectedStatementHo7,
                            expectedStatementHo8,
                            expectedStatementHo9
                    )
            ));

            assertTrue(graphMap.containsKey(expectedHo6));
            assertTrue(graphMap.get(expectedHo6).isEmpty());
        }
    }
}