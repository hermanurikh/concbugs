package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
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
    private ClassFinderService classFinderService;

    private GraphService graphService;

    @BeforeEach
    void init() {
        graphService = new GraphService(classFinderService);
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
            HeapObject ho1 = new HeapObject(point1, "int");

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, "java.lang.String");

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

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
                                    "int"
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
            HeapObject ho1 = new HeapObject(point1, "int");

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, "java.lang.String");

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

            ProgramPoint point4 = new ProgramPoint("d", 4);
            HeapObject ho4 = new HeapObject(point4, "java.lang.Object");

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
            HeapObject ho1 = new HeapObject(point1, "int");

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, "java.lang.String");

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

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
            HeapObject ho1 = new HeapObject(point1, "int");

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, "java.lang.String");

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

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
                                    "int"
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
            HeapObject ho1 = new HeapObject(point1, "int");

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, "java.lang.String");

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

            ProgramPoint point4 = new ProgramPoint("d", 4);
            HeapObject ho4 = new HeapObject(point4, "java.lang.Object");

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
            HeapObject ho1 = new HeapObject(point1, "int");

            ProgramPoint point2 = new ProgramPoint("b", 2);
            HeapObject ho2 = new HeapObject(point2, "java.lang.String");

            ProgramPoint point3 = new ProgramPoint("c", 3);
            HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

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
                                    new ProgramPoint("1", 1), "int"
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

            HeapObject ho1 = new HeapObject(new ProgramPoint("v1", 32), "com.qbutton.concbugs.algorythm.dto.statement.Statement");
            HeapObject ho2 = new HeapObject(new ProgramPoint("v2", 33), "java.lang.String");
            HeapObject ho3 = new HeapObject(new ProgramPoint("v3", 34), "int");
            HeapObject ho4 = new HeapObject(new ProgramPoint("v4", 35), "com.qbutton.concbugs.algorythm.dto.statement.Statement");
            HeapObject ho5 = new HeapObject(new ProgramPoint("v5", 36), "java.lang.Double");
            HeapObject ho6 = new HeapObject(new ProgramPoint("v6", 37), "java.lang.Long");

            when(classFinderService.getSubclassesOf(any())).thenAnswer(invocation -> {
                String clazz = (String) invocation.getArguments()[0];

                if (clazz.equals("com.qbutton.concbugs.algorythm.dto.statement.Statement")) {
                    return ImmutableSet.of(
                            "com.qbutton.concbugs.algorythm.dto.statement.Statement",
                            "com.qbutton.concbugs.algorythm.dto.statement.BranchStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.MethodStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement",
                            "com.qbutton.concbugs.algorythm.dto.statement.WaitStatement"
                    );
                }

                if (clazz.equals("java.lang.String")) {
                    return ImmutableSet.of("java.lang.String");
                }

                if (clazz.equals("int")) {
                    return ImmutableSet.of("int");
                }

                if (clazz.equals("java.lang.Double")) {
                    return ImmutableSet.of("java.lang.Double");
                }

                if (clazz.equals("java.lang.Long")) {
                    return ImmutableSet.of("java.lang.Long");
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

            HeapObject expectedStatementHo1 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.Statement");
            HeapObject expectedStatementHo2 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.BranchStatement");
            HeapObject expectedStatementHo3 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement");
            HeapObject expectedStatementHo4 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement");
            HeapObject expectedStatementHo5 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement");
            HeapObject expectedStatementHo6 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.MethodStatement");
            HeapObject expectedStatementHo7 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement");
            HeapObject expectedStatementHo8 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement");
            HeapObject expectedStatementHo9 = new HeapObject(ProgramPoint.UNKNOWN, "com.qbutton.concbugs.algorythm.dto.statement.WaitStatement");
            HeapObject expectedHo2 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.String");
            HeapObject expectedHo3 = new HeapObject(ProgramPoint.UNKNOWN, "int");
            HeapObject expectedHo5 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Double");
            HeapObject expectedHo6 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Long");
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