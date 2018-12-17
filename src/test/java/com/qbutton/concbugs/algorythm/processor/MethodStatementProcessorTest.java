package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import com.qbutton.concbugs.algorythm.service.MergeService;
import com.qbutton.concbugs.algorythm.service.StateService;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MethodStatementProcessor")
class MethodStatementProcessorTest {

    @Mock
    private VisitorService visitorService;
    @Mock
    private StateService stateService;
    @Mock
    private MergeService mergeService;

    private MethodStatementProcessor methodStatementProcessor;

    @BeforeEach
    void init() {
        methodStatementProcessor = new MethodStatementProcessor(visitorService, stateService, mergeService);
    }

    @Test
    @DisplayName("processes successfully when there already is env with given varName")
    void process_success_envWithVarNameExists() {
        //given
        State originalState = Mockito.mock(State.class);
        String varName = "myVar";
        MethodStatement methodStatement
                = new MethodStatement(32, varName, Collections.emptyList(), Integer.class);
        when(originalState.getEnvironment()).thenReturn(ImmutableList.of(
                new EnvEntry(varName, new HeapObject(ProgramPoint.UNKNOWN, Object.class))));

        //when
        State resultState = methodStatementProcessor.process(methodStatement, originalState);

        //then
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class));
        assertThat(resultState.getEnvironment().size(), is(1));
        assertThat(resultState.getEnvironment().get(0), is(expectedEntry));
    }

    @Test
    @DisplayName("processes successfully when there is no env with given varName")
    void process_success_envWithVarNameDoesNotExist() {
        //given
        State originalState = Mockito.mock(State.class);
        String varName = "myVar";
        MethodStatement methodStatement
                = new MethodStatement(32, varName, Collections.emptyList(), Integer.class);
        EnvEntry originalEnvEntry = new EnvEntry("someVar", new HeapObject(ProgramPoint.UNKNOWN, Object.class));
        when(originalState.getEnvironment()).thenReturn(ImmutableList.of(originalEnvEntry));

        //when
        State resultState = methodStatementProcessor.process(methodStatement, originalState);

        //then
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class));
        assertThat(resultState.getEnvironment().size(), is(2));
        assertThat(resultState.getEnvironment().get(0), is(originalEnvEntry));
        assertThat(resultState.getEnvironment().get(1), is(expectedEntry));
    }

    @Test
    @DisplayName("processes successfully when locks are empty")
    void process_success_locksAreEmpty() {
        //given
        String varName = "myVar";
        MethodDeclaration first = Mockito.mock(MethodDeclaration.class);
        MethodDeclaration second = Mockito.mock(MethodDeclaration.class);
        State returnedMethodState1 = Mockito.mock(State.class);
        State returnedMethodState2 = Mockito.mock(State.class);
        MethodStatement methodStatement
                = new MethodStatement(32, varName, ImmutableList.of(first, second), Integer.class);
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class));

        HeapObject ho1 = new HeapObject(ProgramPoint.UNKNOWN, String.class);
        HeapObject ho2 = new HeapObject(ProgramPoint.UNKNOWN, Integer.class);
        HeapObject ho3 = new HeapObject(ProgramPoint.UNKNOWN, Object.class);
        HeapObject ho4 = new HeapObject(ProgramPoint.UNKNOWN, Number.class);

        Graph originalGraph = new Graph(ImmutableMap.of(ho1, emptySet(),
                ho2, ImmutableSet.of(ho1)));
        Set<HeapObject> originalRoots = ImmutableSet.of(ho3);
        List<HeapObject> originalLocks = Collections.emptyList();
        List<EnvEntry> originalEnv = Collections.emptyList();
        Set<HeapObject> originalWaits = ImmutableSet.of(ho4, ho2);

        State originalState = new State(
                originalGraph, originalRoots, originalLocks, originalEnv, originalWaits
        );

        List<EnvEntry> newEnv = ImmutableList.of(expectedEntry);
        State currentState1 = new State(
                originalGraph, originalRoots, originalLocks, newEnv, originalWaits
        );

        State renamedState1 = Mockito.mock(State.class);
        Graph emptyGraph = new Graph(Collections.emptyMap());
        when(renamedState1.getGraph()).thenReturn(emptyGraph);
        when(renamedState1.getRoots()).thenReturn(ImmutableSet.of(ho2, ho3));
        when(renamedState1.getWaits()).thenReturn(ImmutableSet.of(ho4, ho1));

        Set<HeapObject> newRoots1 = ImmutableSet.of(ho2, ho3);
        Set<HeapObject> newWaits1 = ImmutableSet.of(ho4, ho1, ho2);

        Graph mergedGraph1 = new Graph(ImmutableMap.of(ho4, emptySet()));
        State currentState2 = new State(
                mergedGraph1, newRoots1, originalState.getLocks(), newEnv,  newWaits1
        );

        State renamedState2 = Mockito.mock(State.class);
        when(renamedState2.getGraph()).thenReturn(emptyGraph);
        when(renamedState2.getRoots()).thenReturn(ImmutableSet.of(ho1));
        when(renamedState2.getWaits()).thenReturn(ImmutableSet.of(ho3));

        Set<HeapObject> newRoots2 = ImmutableSet.of(ho2, ho3, ho1);
        Set<HeapObject> newWaits2 = ImmutableSet.of(ho4, ho1, ho2, ho3);

        Graph mergedGraph2 = new Graph(ImmutableMap.of(ho1, emptySet()));
        State currentStateFinal = new State(
                mergedGraph2, newRoots2, originalState.getLocks(), newEnv,  newWaits2
        );


        mockCommonInvocations(first, second, returnedMethodState1, returnedMethodState2, currentState1, renamedState1, emptyGraph, mergedGraph1, currentState2, renamedState2, mergedGraph2);

        //when
        State resultState = methodStatementProcessor.process(methodStatement, originalState);

        //then
        assertThat(resultState, is(currentStateFinal));
    }

    @Test
    @DisplayName("processes successfully when locks are not empty")
    void process_success_locksAreNotEmpty() {
        //given
        String varName = "myVar";
        MethodDeclaration first = Mockito.mock(MethodDeclaration.class);
        MethodDeclaration second = Mockito.mock(MethodDeclaration.class);
        State returnedMethodState1 = Mockito.mock(State.class);
        State returnedMethodState2 = Mockito.mock(State.class);
        MethodStatement methodStatement
                = new MethodStatement(32, varName, ImmutableList.of(first, second), Integer.class);
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class));

        HeapObject ho1 = new HeapObject(ProgramPoint.UNKNOWN, String.class);
        HeapObject ho2 = new HeapObject(ProgramPoint.UNKNOWN, Integer.class);
        HeapObject ho3 = new HeapObject(ProgramPoint.UNKNOWN, Object.class);
        HeapObject ho4 = new HeapObject(ProgramPoint.UNKNOWN, Number.class);
        HeapObject ho5 = new HeapObject(ProgramPoint.UNKNOWN, List.class);

        Graph originalGraph = new Graph(ImmutableMap.of(ho1, emptySet(),
                ho2, ImmutableSet.of(ho1)));
        Set<HeapObject> originalRoots = ImmutableSet.of(ho3);
        List<HeapObject> originalLocks = ImmutableList.of(ho4, ho1);
        List<EnvEntry> originalEnv = Collections.emptyList();
        Set<HeapObject> originalWaits = ImmutableSet.of(ho4, ho2);

        State originalState = new State(
                originalGraph, originalRoots, originalLocks, originalEnv, originalWaits
        );

        List<EnvEntry> newEnv = ImmutableList.of(expectedEntry);
        State currentState1 = new State(
                originalGraph, originalRoots, originalLocks, newEnv, originalWaits
        );

        State renamedState1 = Mockito.mock(State.class);
        Graph emptyGraph = new Graph(Collections.emptyMap());
        when(renamedState1.getGraph()).thenReturn(emptyGraph);
        when(renamedState1.getRoots()).thenReturn(ImmutableSet.of(ho2, ho3));
        when(renamedState1.getWaits()).thenReturn(ImmutableSet.of(ho4, ho1));

        Graph mergedGraph1 = new Graph(ImmutableMap.of(ho1, emptySet()));

        Graph newGraph1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2, ho3, ho4),
                ho2, emptySet(),
                ho3, emptySet(),
                ho4, emptySet()
        ));

        State currentState2 = new State(
                newGraph1, originalRoots, originalState.getLocks(), newEnv,  originalWaits
        );

        State renamedState2 = Mockito.mock(State.class);
        when(renamedState2.getGraph()).thenReturn(emptyGraph);
        when(renamedState2.getRoots()).thenReturn(ImmutableSet.of(ho5));
        when(renamedState2.getWaits()).thenReturn(ImmutableSet.of(ho5, ho4));


        Graph mergedGraph2 = new Graph(ImmutableMap.of(ho1, emptySet()));

        Graph newGraph2 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho5, ho4),
                ho5, emptySet(),
                ho4, emptySet()
        ));

        State currentStateFinal = new State(
                newGraph2, originalRoots, originalState.getLocks(), newEnv,  originalWaits
        );


        mockCommonInvocations(first, second,
                returnedMethodState1, returnedMethodState2,
                currentState1, renamedState1,
                emptyGraph, mergedGraph1, currentState2, renamedState2, mergedGraph2);

        //when
        State resultState = methodStatementProcessor.process(methodStatement, originalState);

        //then
        assertThat(resultState, is(currentStateFinal));
    }

    @Test
    @DisplayName("fails when last lock is not in graph")
    void process_fail_lockIsNotInGraph() {
        //given
        String varName = "myVar";
        MethodDeclaration first = Mockito.mock(MethodDeclaration.class);
        MethodDeclaration second = Mockito.mock(MethodDeclaration.class);
        State returnedMethodState1 = Mockito.mock(State.class);
        State returnedMethodState2 = Mockito.mock(State.class);
        MethodStatement methodStatement
                = new MethodStatement(32, varName, ImmutableList.of(first, second), Integer.class);
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class));

        HeapObject ho1 = new HeapObject(ProgramPoint.UNKNOWN, String.class);
        HeapObject ho2 = new HeapObject(ProgramPoint.UNKNOWN, Integer.class);
        HeapObject ho3 = new HeapObject(ProgramPoint.UNKNOWN, Object.class);
        HeapObject ho4 = new HeapObject(ProgramPoint.UNKNOWN, Number.class);

        Graph originalGraph = new Graph(ImmutableMap.of(ho1, emptySet(),
                ho2, ImmutableSet.of(ho1)));
        Set<HeapObject> originalRoots = ImmutableSet.of(ho3);
        List<HeapObject> originalLocks = ImmutableList.of(ho4, ho1);
        List<EnvEntry> originalEnv = Collections.emptyList();
        Set<HeapObject> originalWaits = ImmutableSet.of(ho4, ho2);

        State originalState = new State(
                originalGraph, originalRoots, originalLocks, originalEnv, originalWaits
        );

        List<EnvEntry> newEnv = ImmutableList.of(expectedEntry);
        State currentState1 = new State(
                originalGraph, originalRoots, originalLocks, newEnv, originalWaits
        );

        State renamedState1 = Mockito.mock(State.class);
        Graph emptyGraph = new Graph(Collections.emptyMap());
        when(renamedState1.getGraph()).thenReturn(emptyGraph);

        Graph mergedGraph1 = new Graph(ImmutableMap.of(ho2, emptySet()));

        Graph newGraph1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2, ho3, ho4),
                ho2, emptySet(),
                ho3, emptySet(),
                ho4, emptySet()
        ));

        State currentState2 = new State(
                newGraph1, originalRoots, originalState.getLocks(), newEnv,  originalWaits
        );

        State renamedState2 = Mockito.mock(State.class);

        Graph mergedGraph2 = new Graph(ImmutableMap.of(ho1, emptySet()));

        mockCommonInvocations(first, second, returnedMethodState1, returnedMethodState2, currentState1, renamedState1, emptyGraph, mergedGraph1, currentState2, renamedState2, mergedGraph2);

        //when
        //then
        assertThrows(AlgorithmValidationException.class, () -> methodStatementProcessor.process(methodStatement, originalState));
    }

    private void mockCommonInvocations(MethodDeclaration first, MethodDeclaration second, State returnedMethodState1, State returnedMethodState2, State currentState1, State renamedState1, Graph emptyGraph, Graph mergedGraph1, State currentState2, State renamedState2, Graph mergedGraph2) {
        mockVisitMethod(first, second, returnedMethodState1, returnedMethodState2);

        mockRenameFromCalleeToCallerContext(returnedMethodState1, returnedMethodState2, currentState1, renamedState1, currentState2, renamedState2);

        mockMergeGraphs(currentState1, emptyGraph, mergedGraph1, currentState2, mergedGraph2);
    }

    private void mockMergeGraphs(State currentState1, Graph emptyGraph, Graph mergedGraph1, State currentState2, Graph mergedGraph2) {
        when(mergeService.mergeGraphs(any(Graph.class), any(Graph.class))).thenAnswer(invocationOnMock -> {
            Graph g1 = (Graph) invocationOnMock.getArguments()[0];
            Graph g2 = (Graph) invocationOnMock.getArguments()[1];
            if (g1.equals(currentState1.getGraph()) && g2.equals(emptyGraph)) return mergedGraph1;
            if (g1.equals(currentState2.getGraph()) && g2.equals(emptyGraph)) return mergedGraph2;
            return null;
        });
    }

    private void mockRenameFromCalleeToCallerContext(State returnedMethodState1, State returnedMethodState2, State currentState1, State renamedState1, State currentState2, State renamedState2) {
        when(stateService.renameFromCalleeToCallerContext(any(State.class), any(State.class))).thenAnswer(invocationOnMock -> {
            State returnedMethodState = (State) invocationOnMock.getArguments()[0];
            State currentState = (State) invocationOnMock.getArguments()[1];
            if (returnedMethodState == returnedMethodState1 && currentState.equals(currentState1)) return renamedState1;
            if (returnedMethodState == returnedMethodState2 && currentState.equals(currentState2)) return renamedState2;
            return null;
        });
    }

    private void mockVisitMethod(MethodDeclaration first, MethodDeclaration second, State returnedMethodState1, State returnedMethodState2) {
        when(visitorService.visitMethod(any(MethodDeclaration.class))).thenAnswer(invocationOnMock -> {
            MethodDeclaration method = (MethodDeclaration) invocationOnMock.getArguments()[0];
            if (method == first) return returnedMethodState1;
            if (method == second) return returnedMethodState2;
            return null;
        });
    }
}