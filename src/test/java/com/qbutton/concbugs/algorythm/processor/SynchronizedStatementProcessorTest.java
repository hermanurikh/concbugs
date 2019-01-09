package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SynchronizedStatementProcessor")
class SynchronizedStatementProcessorTest {

    @Mock
    private VisitorService visitorService;

    private SynchronizedStatementProcessor synchronizedStatementProcessor;

    private static final String METHOD_NAME = "bar";

    @BeforeEach
    void init() {
        synchronizedStatementProcessor = new SynchronizedStatementProcessor(visitorService);
    }

    @Test
    @DisplayName("processes correctly when synchronization object is already locked")
    void process_success_objectIsAlreadyLocked() {
        //given
        int offset = 34;
        String varName = "this";
        WaitStatement body = new WaitStatement(35, "abc");
        String className = "java.lang.String";
        SynchronizedStatement statement = new SynchronizedStatement(offset, varName, body, className, METHOD_NAME);
        HeapObject ho1 = new HeapObject(new ProgramPoint("v1", 2), "int", METHOD_NAME, "l1");
        HeapObject ho2 = new HeapObject(new ProgramPoint("v2", 3), "java.lang.String", METHOD_NAME, "l2");
        HeapObject ho3 = new HeapObject(new ProgramPoint("v3", 4), "java.lang.Number", METHOD_NAME, "l3");
        Graph graph = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2, ho3),
                ho2, ImmutableSet.of(ho3),
                ho3, Collections.emptySet()
        ));
        Set<HeapObject> roots = ImmutableSet.of(ho3);
        List<HeapObject> locks = ImmutableList.of(ho2, ho1);
        List<EnvEntry> envs = ImmutableList.of(new EnvEntry("v2", ho2));
        Set<HeapObject> waits = ImmutableSet.of(ho1);
        State visitResultState = new State(graph, roots, locks, envs, waits);

        List<EnvEntry> envEntries = ImmutableList.of(new EnvEntry(varName, ho1));

        State initialState = new State(
                new Graph(emptyMap()),
                emptySet(),
                ImmutableList.of(ho1),
                envEntries,
                emptySet()
        );
        when(visitorService.visitStatement(eq(body), eq(initialState))).thenReturn(visitResultState);

        //when
        State result = synchronizedStatementProcessor.process(statement, initialState);

        //then
        verify(visitorService).visitStatement(eq(body), eq(initialState));
        assertThat(result.getGraph(), is(graph));
        assertThat(result.getRoots(), is(roots));
        assertThat(result.getLocks(), is(ImmutableList.of(ho1)));
        assertThat(result.getEnvironment(), is(envs));
        assertThat(result.getWaits(), is(waits));
    }

    @Test
    @DisplayName("processes correctly when some lock is already held")
    void process_success_someLockIsHeld() {
        //given
        int offset = 34;
        String varName = "this";
        WaitStatement body = new WaitStatement(35, "abc");
        String className = "java.lang.String";
        SynchronizedStatement statement = new SynchronizedStatement(offset, varName, body, className, METHOD_NAME);
        HeapObject ho1 = new HeapObject(new ProgramPoint("v1", 2), "int", METHOD_NAME, "l1");
        HeapObject ho2 = new HeapObject(new ProgramPoint("v2", 3), "java.lang.String", METHOD_NAME, "l2");
        HeapObject ho3 = new HeapObject(new ProgramPoint("v3", 4), "java.lang.Number", METHOD_NAME, "l3");
        Graph graph1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2),
                ho2, ImmutableSet.of(ho1)
        ));
        Set<HeapObject> roots1 = ImmutableSet.of(ho3);
        List<HeapObject> locks1 = emptyList();
        List<EnvEntry> envs1 = ImmutableList.of(new EnvEntry(varName, ho3));
        Set<HeapObject> waits1 = ImmutableSet.of(ho1);
        State initialState = new State(graph1, roots1, locks1, envs1, waits1);

        State stateAfterMerge = new State(
                new Graph(ImmutableMap.of(
                        ho1, ImmutableSet.of(ho2),
                        ho2, ImmutableSet.of(ho1),
                        ho3, Collections.emptySet())),
                roots1,
                ImmutableList.of(ho3),
                envs1,
                waits1
        );

        Graph graph2 = new Graph(ImmutableMap.of(
                ho3, Collections.emptySet()
        ));
        Set<HeapObject> roots2 = ImmutableSet.of(ho1);
        List<HeapObject> locks2 = ImmutableList.of(ho2);
        List<EnvEntry> envs2 = ImmutableList.of(new EnvEntry("v3", ho3));
        Set<HeapObject> waits2 = ImmutableSet.of(ho3);

        State visitorState = new State(graph2, roots2, locks2, envs2, waits2);
        when(visitorService.visitStatement(eq(body), eq(stateAfterMerge))).thenReturn(visitorState);

        //when
        State result = synchronizedStatementProcessor.process(statement, initialState);

        //then
        verify(visitorService).visitStatement(eq(body), eq(stateAfterMerge));
        assertThat(result.getGraph(), is(graph2));
        assertThat(result.getRoots(), is(roots2));
        assertThat(result.getLocks(), is(emptyList()));
        assertThat(result.getEnvironment(), is(envs2));
        assertThat(result.getWaits(), is(waits2));
    }

    @Test
    @DisplayName("processes correctly when no locks are already held")
    void process_success_noLockIsHeld() {
        //given
        int offset = 34;
        String varName = "this";
        WaitStatement body = new WaitStatement(35, "abc");
        String className = "java.lang.String";
        SynchronizedStatement statement = new SynchronizedStatement(offset, varName, body, className, METHOD_NAME);
        HeapObject ho1 = new HeapObject(new ProgramPoint("v1", 2), "int", METHOD_NAME, "l1");
        HeapObject ho2 = new HeapObject(new ProgramPoint("v2", 3), "java.lang.String", METHOD_NAME, "l2");
        HeapObject ho3 = new HeapObject(new ProgramPoint("v3", 4), "java.lang.Number", METHOD_NAME, "l3");
        Graph graph1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2),
                ho2, ImmutableSet.of(ho1)
        ));
        Set<HeapObject> roots1 = ImmutableSet.of(ho3);
        List<HeapObject> locks1 = ImmutableList.of(ho2);
        List<EnvEntry> envs1 = ImmutableList.of(new EnvEntry(varName, ho3));
        Set<HeapObject> waits1 = ImmutableSet.of(ho1);
        State initialState = new State(graph1, roots1, locks1, envs1, waits1);

        State stateAfterMerge = new State(
                new Graph(ImmutableMap.of(
                        ho1, ImmutableSet.of(ho2),
                        ho2, ImmutableSet.of(ho1, ho3),
                        ho3, Collections.emptySet())),
                roots1,
                ImmutableList.of(ho2, ho3),
                envs1,
                waits1
        );

        Graph graph2 = new Graph(ImmutableMap.of(
                ho3, Collections.emptySet()
        ));
        Set<HeapObject> roots2 = ImmutableSet.of(ho1);
        List<HeapObject> locks2 = ImmutableList.of(ho2);
        List<EnvEntry> envs2 = ImmutableList.of(new EnvEntry("v3", ho3));
        Set<HeapObject> waits2 = ImmutableSet.of(ho3);

        State visitorState = new State(graph2, roots2, locks2, envs2, waits2);
        when(visitorService.visitStatement(eq(body), eq(stateAfterMerge))).thenReturn(visitorState);

        //when
        State result = synchronizedStatementProcessor.process(statement, initialState);

        //then
        verify(visitorService).visitStatement(eq(body), eq(stateAfterMerge));
        assertThat(result.getGraph(), is(graph2));
        assertThat(result.getRoots(), is(roots2));
        assertThat(result.getLocks(), is(locks1));
        assertThat(result.getEnvironment(), is(envs2));
        assertThat(result.getWaits(), is(waits2));
    }

    @Test
    @DisplayName("processes correctly when no locks are already held, and synchronized object is already in graph")
    void process_success_noLockIsHeld_graphMerge() {
        //given
        int offset = 34;
        String varName = "this";
        WaitStatement body = new WaitStatement(35, "abc");
        String className = "java.lang.String";
        SynchronizedStatement statement = new SynchronizedStatement(offset, varName, body, className, METHOD_NAME);
        HeapObject ho1 = new HeapObject(new ProgramPoint("v1", 2), "int", METHOD_NAME, "l1");
        HeapObject ho2 = new HeapObject(new ProgramPoint("v2", 3), "java.lang.String", METHOD_NAME, "l2");
        HeapObject ho3 = new HeapObject(new ProgramPoint("v3", 4), "java.lang.Number", METHOD_NAME, "l3");
        Graph graph1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2),
                ho2, ImmutableSet.of(ho1),
                ho3, ImmutableSet.of(ho1)
        ));
        Set<HeapObject> roots1 = ImmutableSet.of(ho3);
        List<HeapObject> locks1 = ImmutableList.of(ho2);
        List<EnvEntry> envs1 = ImmutableList.of(new EnvEntry(varName, ho3));
        Set<HeapObject> waits1 = ImmutableSet.of(ho1);
        State initialState = new State(graph1, roots1, locks1, envs1, waits1);

        State stateAfterMerge = new State(
                new Graph(ImmutableMap.of(
                        ho1, ImmutableSet.of(ho2),
                        ho2, ImmutableSet.of(ho1, ho3),
                        ho3, ImmutableSet.of(ho1))),
                roots1,
                ImmutableList.of(ho2, ho3),
                envs1,
                waits1
        );

        Graph graph2 = new Graph(ImmutableMap.of(
                ho3, Collections.emptySet()
        ));
        Set<HeapObject> roots2 = ImmutableSet.of(ho1);
        List<HeapObject> locks2 = ImmutableList.of(ho2);
        List<EnvEntry> envs2 = ImmutableList.of(new EnvEntry("v3", ho3));
        Set<HeapObject> waits2 = ImmutableSet.of(ho3);

        State visitorState = new State(graph2, roots2, locks2, envs2, waits2);
        when(visitorService.visitStatement(eq(body), eq(stateAfterMerge))).thenReturn(visitorState);

        //when
        State result = synchronizedStatementProcessor.process(statement, initialState);

        //then
        verify(visitorService).visitStatement(eq(body), eq(stateAfterMerge));
        assertThat(result.getGraph(), is(graph2));
        assertThat(result.getRoots(), is(roots2));
        assertThat(result.getLocks(), is(locks1));
        assertThat(result.getEnvironment(), is(envs2));
        assertThat(result.getWaits(), is(waits2));
    }

    @Test
    @DisplayName("works correctly when object with given variable name is not found in env")
    void process_success_objectNotInEnv() {
        //given
        int offset = 34;
        String varName = "this";
        WaitStatement body = new WaitStatement(35, "abc");
        String className = "java.lang.String";
        SynchronizedStatement statement = new SynchronizedStatement(offset, varName, body, className, METHOD_NAME);
        State emptyState = new State(
                new Graph(emptyMap()),
                emptySet(),
                emptyList(),
                emptyList(),
                emptySet()
        );
        ArgumentCaptor<State> stateArgumentCaptor = ArgumentCaptor.forClass(State.class);
        when(visitorService.visitStatement(any(), stateArgumentCaptor.capture())).thenReturn(emptyState);

        //when
        State processResult = synchronizedStatementProcessor.process(statement, emptyState);
        //then
        State capturedState = stateArgumentCaptor.getValue();
        assertThat(processResult, is(emptyState));
        assertThat(capturedState.getEnvironment().size(), is(1));
        assertThat(capturedState.getEnvironment().get(0).getHeapObject().getClazz(), is(className));
        assertThat(capturedState.getEnvironment().get(0).getHeapObject().getProgramPoint(), is(ProgramPoint.UNKNOWN));
        assertThat(capturedState.getEnvironment().get(0).getVarName(), is("this"));
    }
}