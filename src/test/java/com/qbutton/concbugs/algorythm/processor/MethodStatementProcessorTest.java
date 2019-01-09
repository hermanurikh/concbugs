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
import com.qbutton.concbugs.algorythm.service.GraphService;
import com.qbutton.concbugs.algorythm.service.MergeService;
import com.qbutton.concbugs.algorythm.service.StateService;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
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
    @Mock
    private GraphService graphService;

    private MethodStatementProcessor methodStatementProcessor;

    private static final String METHOD_NAME = "some method";

    @BeforeEach
    void init() {
        methodStatementProcessor = new MethodStatementProcessor(visitorService, stateService, mergeService, graphService);
        doCallRealMethod().when(graphService).addOrReplaceEnv(any(), any());
    }

    @Test
    @DisplayName("processes successfully when there already is env with given varName")
    @Disabled("empty method declarations conflict with current method logic")
    void process_success_envWithVarNameExists() {
        //given
        State originalState = Mockito.mock(State.class);
        String varName = "myVar";
        MethodStatement methodStatement
                = new MethodStatement(32, varName, Collections.emptyList(), "int", ImmutableList.of("varName"));
        when(originalState.getEnvironment()).thenReturn(ImmutableList.of(
                new EnvEntry(varName, new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Object", METHOD_NAME, varName))));

        //when
        State resultState = methodStatementProcessor.process(methodStatement, originalState);

        //then
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), "int", METHOD_NAME, varName));
        assertThat(resultState.getEnvironment().size(), is(1));
        assertThat(resultState.getEnvironment().get(0), is(expectedEntry));
    }

    @Test
    @DisplayName("processes successfully when there is no env with given varName")
    @Disabled("empty method declarations conflict with current method logic")
    void process_success_envWithVarNameDoesNotExist() {
        //given
        State originalState = Mockito.mock(State.class);
        String varName = "myVar";
        MethodStatement methodStatement
                = new MethodStatement(32, varName,
                ImmutableList.of(new MethodDeclaration("name", emptyList(), null, 2)),
                "int", ImmutableList.of("someVar"));
        EnvEntry originalEnvEntry = new EnvEntry("someVar", new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Object", METHOD_NAME, "someVar2"));
        when(originalState.getEnvironment()).thenReturn(ImmutableList.of(originalEnvEntry));

        //when
        State resultState = methodStatementProcessor.process(methodStatement, originalState);

        //then
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), "int", METHOD_NAME, "someVar2"));
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
        when(first.getMethodName()).thenReturn(METHOD_NAME);
        MethodDeclaration second = Mockito.mock(MethodDeclaration.class);
        State returnedMethodState1 = Mockito.mock(State.class);
        State returnedMethodState2 = Mockito.mock(State.class);
        MethodStatement methodStatement
                = new MethodStatement(32, varName, ImmutableList.of(first, second), "int", emptyList());
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), "int", METHOD_NAME, varName));

        HeapObject ho1 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.String", METHOD_NAME, "1");
        HeapObject ho2 = new HeapObject(ProgramPoint.UNKNOWN, "int", METHOD_NAME, "2");
        HeapObject ho3 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Object", METHOD_NAME, "3");
        HeapObject ho4 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Number", METHOD_NAME, "4");

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
                mergedGraph1, newRoots1, originalState.getLocks(), newEnv, newWaits1
        );

        State renamedState2 = Mockito.mock(State.class);
        when(renamedState2.getGraph()).thenReturn(emptyGraph);
        when(renamedState2.getRoots()).thenReturn(ImmutableSet.of(ho1));
        when(renamedState2.getWaits()).thenReturn(ImmutableSet.of(ho3));

        Set<HeapObject> newRoots2 = ImmutableSet.of(ho2, ho3, ho1);
        Set<HeapObject> newWaits2 = ImmutableSet.of(ho4, ho1, ho2, ho3);

        Graph mergedGraph2 = new Graph(ImmutableMap.of(ho1, emptySet()));
        State currentStateFinal = new State(
                mergedGraph2, newRoots2, originalState.getLocks(), newEnv, newWaits2
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
        when(first.getMethodName()).thenReturn(METHOD_NAME);

        State returnedMethodState1 = Mockito.mock(State.class);
        State returnedMethodState2 = Mockito.mock(State.class);
        MethodStatement methodStatement
                = new MethodStatement(32, varName, ImmutableList.of(first, second), "int", emptyList());
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), "int", METHOD_NAME, varName));

        HeapObject ho1 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.String", METHOD_NAME, "1");
        HeapObject ho2 = new HeapObject(ProgramPoint.UNKNOWN, "int", METHOD_NAME, "2");
        HeapObject ho3 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Object", METHOD_NAME, "3");
        HeapObject ho4 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Number", METHOD_NAME, "4");
        HeapObject ho5 = new HeapObject(ProgramPoint.UNKNOWN, "java.util.List", METHOD_NAME,"5");

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
                newGraph1, originalRoots, originalState.getLocks(), newEnv, originalWaits
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
                newGraph2, originalRoots, originalState.getLocks(), newEnv, originalWaits
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

        when(first.getMethodName()).thenReturn(METHOD_NAME);

        State returnedMethodState1 = Mockito.mock(State.class);
        State returnedMethodState2 = Mockito.mock(State.class);
        MethodStatement methodStatement
                = new MethodStatement(32, varName, ImmutableList.of(first, second), "int", emptyList());
        EnvEntry expectedEntry = new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), "int", METHOD_NAME, varName));

        HeapObject ho1 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.String", METHOD_NAME, "1");
        HeapObject ho2 = new HeapObject(ProgramPoint.UNKNOWN, "int", METHOD_NAME, "2");
        HeapObject ho3 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Object", METHOD_NAME, "3");
        HeapObject ho4 = new HeapObject(ProgramPoint.UNKNOWN, "java.lang.Number", METHOD_NAME, "4");

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
                newGraph1, originalRoots, originalState.getLocks(), newEnv, originalWaits
        );

        State renamedState2 = Mockito.mock(State.class);

        Graph mergedGraph2 = new Graph(ImmutableMap.of(ho1, emptySet()));

        mockCommonInvocations(first, second, returnedMethodState1, returnedMethodState2, currentState1, renamedState1, emptyGraph, mergedGraph1, currentState2, renamedState2, mergedGraph2);

        //when
        //then
        assertThrows(AlgorithmValidationException.class, () -> methodStatementProcessor.process(methodStatement, originalState));
    }

    @Test
    @DisplayName("processes successfully when actual parameters correspond to formal ones")
    void process_success_actualParamsAreOk() {
        //given
        String firstVarName = "hey";
        String secondVarName = "hoe";
        HeapObject ho1 = new HeapObject(new ProgramPoint(firstVarName, 23), "firstClass", METHOD_NAME, firstVarName);
        HeapObject ho2 = new HeapObject(new ProgramPoint(secondVarName, 24), "secondClass", METHOD_NAME, secondVarName);
        List<EnvEntry> originalEnv = ImmutableList.of(
                new EnvEntry(firstVarName, ho1),
                new EnvEntry(secondVarName, ho2)
        );
        State originalState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                originalEnv,
                emptySet()
        );

        String methodReturnVarName = "some return var";

        List<EnvEntry> newEnv = ImmutableList.of(
                new EnvEntry(firstVarName, ho1),
                new EnvEntry(secondVarName, ho2),
                new EnvEntry(methodReturnVarName, new HeapObject(new ProgramPoint(methodReturnVarName, 43), "int", METHOD_NAME, methodReturnVarName))
        );

        State currentState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                newEnv,
                emptySet()
        );

        MethodDeclaration methodDeclaration = new MethodDeclaration("some method",
                ImmutableList.of(new MethodDeclaration.Variable("lil", "actualClass")),
                null, 104);

        MethodStatement methodStatement = new MethodStatement(
                43, methodReturnVarName,
                ImmutableList.of(methodDeclaration), "int", ImmutableList.of(secondVarName)
        );

        HeapObject expectedFormalHo = new HeapObject(new ProgramPoint("lil", 333), "actuallClassFromEnv", METHOD_NAME, "lil2");
        List<EnvEntry> returnedEnv = ImmutableList.of(
                new EnvEntry("lil", expectedFormalHo),
                new EnvEntry("lil2", new HeapObject(ProgramPoint.UNKNOWN, "someOtherClass", METHOD_NAME, "lil3"))
        );

        State returnedMethodState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                returnedEnv,
                emptySet()
        );

        List<HeapObject> expectedFormalParameters = ImmutableList.of(expectedFormalHo);
        List<HeapObject> expectedActualParameters = ImmutableList.of(ho2);

        when(visitorService.visitMethod(methodDeclaration)).thenReturn(returnedMethodState);
        when(stateService.renameFromCalleeToCallerContext(
                returnedMethodState, currentState, expectedFormalParameters, expectedActualParameters))
                .thenReturn(State.EMPTY_STATE);
        when(mergeService.mergeGraphs(any(), any())).thenReturn(new Graph(emptyMap()));

        //when
        methodStatementProcessor.process(methodStatement, originalState);

        //then

        verify(stateService).renameFromCalleeToCallerContext(
                returnedMethodState, currentState, expectedFormalParameters, expectedActualParameters);
    }

    @Test
    @DisplayName("processes successfully when actual parameters are empty")
    void process_success_actualParamsAreEmpty() {
        //given
        List<EnvEntry> originalEnv = emptyList();
        State originalState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                originalEnv,
                emptySet()
        );

        String methodReturnVarName = "some return var";

        List<EnvEntry> newEnv = ImmutableList.of(
                new EnvEntry(methodReturnVarName, new HeapObject(new ProgramPoint(methodReturnVarName, 43), "int", "some method", methodReturnVarName))
        );

        State currentState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                newEnv,
                emptySet()
        );

        MethodDeclaration methodDeclaration = new MethodDeclaration("some method",
                ImmutableList.of(new MethodDeclaration.Variable("lil", "actualClass")),
                null, 104);

        MethodStatement methodStatement = new MethodStatement(
                43, methodReturnVarName,
                ImmutableList.of(methodDeclaration), "int", emptyList()
        );

        HeapObject expectedFormalHo = new HeapObject(new ProgramPoint("lil", 333), "actuallClassFromEnv", "some method", "lil4");
        List<EnvEntry> returnedEnv = ImmutableList.of(
                new EnvEntry("lil", expectedFormalHo),
                new EnvEntry("lil2", new HeapObject(ProgramPoint.UNKNOWN, "someOtherClass", "some method", "lil5"))
        );

        State returnedMethodState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                returnedEnv,
                emptySet()
        );

        List<HeapObject> expectedFormalParameters = ImmutableList.of(expectedFormalHo);
        List<HeapObject> expectedActualParameters = ImmutableList.of(new HeapObject(ProgramPoint.UNKNOWN, "actuallClassFromEnv", "some method", "lil4"));

        when(visitorService.visitMethod(methodDeclaration)).thenReturn(returnedMethodState);
        when(stateService.renameFromCalleeToCallerContext(
                returnedMethodState, currentState, expectedFormalParameters, expectedActualParameters))
                .thenReturn(State.EMPTY_STATE);
        when(mergeService.mergeGraphs(any(), any())).thenReturn(new Graph(emptyMap()));

        //when
        methodStatementProcessor.process(methodStatement, originalState);

        //then
        verify(stateService).renameFromCalleeToCallerContext(
                returnedMethodState, currentState, expectedFormalParameters, expectedActualParameters);
    }

    @Test
    @DisplayName("throws exception when original env does not contain var of actual param")
    void process_fail_originalEnvWithoutActualParam() {
        //given
        List<EnvEntry> originalEnv = emptyList();
        State originalState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                originalEnv,
                emptySet()
        );

        String methodReturnVarName = "some return var";

        List<EnvEntry> newEnv = ImmutableList.of(
                new EnvEntry(methodReturnVarName, new HeapObject(new ProgramPoint(methodReturnVarName, 43), "int", METHOD_NAME, methodReturnVarName))
        );

        State currentState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                newEnv,
                emptySet()
        );

        MethodDeclaration methodDeclaration = new MethodDeclaration("some method",
                ImmutableList.of(new MethodDeclaration.Variable("lil", "actualClass")),
                null, 104);

        MethodStatement methodStatement = new MethodStatement(
                43, methodReturnVarName,
                ImmutableList.of(methodDeclaration), "int", ImmutableList.of("unknown")
        );

        HeapObject expectedFormalHo = new HeapObject(new ProgramPoint("lil", 333), "actuallClassFromEnv", METHOD_NAME, "lil5");
        List<EnvEntry> returnedEnv = ImmutableList.of(
                new EnvEntry("lil", expectedFormalHo),
                new EnvEntry("lil2", new HeapObject(ProgramPoint.UNKNOWN, "someOtherClass", METHOD_NAME, "lil2"))
        );

        State returnedMethodState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                returnedEnv,
                emptySet()
        );

        when(visitorService.visitMethod(methodDeclaration)).thenReturn(returnedMethodState);

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
        when(stateService.renameFromCalleeToCallerContext(any(State.class), any(State.class), any(), any())).thenAnswer(invocationOnMock -> {
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