package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("WaitStatementProcessor")
class WaitStatementProcessorTest {

    private static final String METHOD_NAME = "bar";

    @Test
    @DisplayName("processes correctly when original locks are empty")
    void process_success_locksAreEmpty() {
        //given
        WaitStatementProcessor processor = new WaitStatementProcessor();
        String waitVarName = "campaignId";
        HeapObject waitHeapObject = new HeapObject(new ProgramPoint(waitVarName, 23), "java.lang.Long", METHOD_NAME, waitVarName);
        HeapObject heapObject = new HeapObject(new ProgramPoint("clientId", 25), "int", METHOD_NAME, "campaignId");
        List<EnvEntry> originalEnv = ImmutableList.of(new EnvEntry(waitVarName, waitHeapObject));

        WaitStatement waitStatement = new WaitStatement(35, waitVarName);
        Graph originalGraph = new Graph(ImmutableMap.of(heapObject, Collections.emptySet()));
        Set<HeapObject> originalRoots = ImmutableSet.of(heapObject);
        State originalState = new State(
                originalGraph,
                originalRoots,
                emptyList(),
                originalEnv,
                ImmutableSet.of(heapObject));
        //when
        State newState = processor.process(waitStatement, originalState);

        //then
        assertThat(newState.getGraph(), is(originalGraph));
        assertThat(newState.getRoots(), is(originalRoots));
        assertThat(newState.getLocks(), is(emptyList()));
        assertThat(newState.getEnvironment(), is(originalEnv));
        assertThat(newState.getWaits().size(), is(2));
        assertTrue(newState.getWaits().contains(heapObject));
        assertTrue(newState.getWaits().contains(waitHeapObject));
    }

    @Test
    @DisplayName("processes correctly when original locks are not empty and last lock is different from wait object")
    void process_success_locksAreNotEmptyAndTailDiffers() {
        //given
        WaitStatementProcessor processor = new WaitStatementProcessor();
        String waitVarName = "campaignId";
        HeapObject waitHeapObject = new HeapObject(new ProgramPoint(waitVarName, 23), "java.lang.Long", METHOD_NAME, waitVarName);
        HeapObject heapObject = new HeapObject(new ProgramPoint("clientId", 25), "int", METHOD_NAME, "campaignId");
        List<EnvEntry> originalEnv = ImmutableList.of(new EnvEntry(waitVarName, waitHeapObject));

        WaitStatement waitStatement = new WaitStatement(35, waitVarName);
        Graph originalGraph = new Graph(ImmutableMap.of(heapObject, Collections.emptySet()));
        Set<HeapObject> originalRoots = ImmutableSet.of(heapObject);
        Set<HeapObject> originalWaits = ImmutableSet.of(heapObject);
        List<HeapObject> originalLocks = ImmutableList.of(
                new HeapObject(
                        new ProgramPoint("some name", 33),
                        "java.lang.String", METHOD_NAME, "new name"),
                heapObject);
        State originalState = new State(
                originalGraph,
                originalRoots,
                originalLocks,
                originalEnv,
                originalWaits);
        //when
        State newState = processor.process(waitStatement, originalState);

        //then
        Map<HeapObject, Set<HeapObject>> newGraph = newState.getGraph().getNeighbors();
        assertThat(newGraph.size(), is(2));
        assertTrue(newGraph.containsKey(waitHeapObject));
        assertTrue(newGraph.containsKey(heapObject));
        assertThat(newGraph.get(heapObject).size(), is(1));
        assertTrue(newGraph.get(heapObject).contains(waitHeapObject));

        assertThat(newState.getRoots(), is(originalRoots));
        assertThat(newState.getLocks(), is(originalLocks));
        assertThat(newState.getEnvironment(), is(originalEnv));
        assertThat(newState.getWaits(), is(originalWaits));
    }

    @Test
    @DisplayName("processes correctly when original locks are not empty and last lock is different from wait object, but wait object is already in graph")
    void process_success_locksAreNotEmptyAndTailDiffers_graphMerge() {
        //given
        WaitStatementProcessor processor = new WaitStatementProcessor();
        String waitVarName = "campaignId";
        HeapObject waitHeapObject = new HeapObject(new ProgramPoint(waitVarName, 23), "java.lang.Long", METHOD_NAME, waitVarName);
        HeapObject heapObject = new HeapObject(new ProgramPoint("clientId", 25), "int", METHOD_NAME, "campId");
        List<EnvEntry> originalEnv = ImmutableList.of(new EnvEntry(waitVarName, waitHeapObject));

        WaitStatement waitStatement = new WaitStatement(35, waitVarName);
        HeapObject heapObject2 = new HeapObject(new ProgramPoint("1", 1), "java.lang.Object", METHOD_NAME, "11");
        HeapObject heapObject3 = new HeapObject(new ProgramPoint("3", 3), "java.lang.Object", METHOD_NAME, "12");
        Graph originalGraph = new Graph(ImmutableMap.of(
                heapObject, ImmutableSet.of(heapObject3),
                waitHeapObject, ImmutableSet.of(heapObject2)));
        Set<HeapObject> originalRoots = ImmutableSet.of(heapObject);
        Set<HeapObject> originalWaits = ImmutableSet.of(heapObject);
        List<HeapObject> originalLocks = ImmutableList.of(
                new HeapObject(
                        new ProgramPoint("some name", 33), "java.lang.String", METHOD_NAME, "new name"),
                heapObject);
        State originalState = new State(
                originalGraph,
                originalRoots,
                originalLocks,
                originalEnv,
                originalWaits);
        //when
        State newState = processor.process(waitStatement, originalState);

        //then
        Map<HeapObject, Set<HeapObject>> newGraph = newState.getGraph().getNeighbors();
        assertThat(newGraph.size(), is(2));
        assertTrue(newGraph.containsKey(waitHeapObject));
        assertTrue(newGraph.containsKey(heapObject));
        assertThat(newGraph.get(heapObject).size(), is(2));
        assertTrue(newGraph.get(heapObject).contains(waitHeapObject));
        assertTrue(newGraph.get(heapObject).contains(heapObject3));
        assertThat(newGraph.get(waitHeapObject).size(), is(1));
        assertTrue(newGraph.get(waitHeapObject).contains(heapObject2));

        assertThat(newState.getRoots(), is(originalRoots));
        assertThat(newState.getLocks(), is(originalLocks));
        assertThat(newState.getEnvironment(), is(originalEnv));
        assertThat(newState.getWaits(), is(originalWaits));
    }

    @Test
    @DisplayName("processes correctly when original locks are not empty and last lock is same as wait object")
    void process_success_locksAreNotEmptyAndTailIsSame() {
        //given
        WaitStatementProcessor processor = new WaitStatementProcessor();
        String waitVarName = "campaignId";
        HeapObject waitHeapObject = new HeapObject(new ProgramPoint(waitVarName, 23), "java.lang.Long", METHOD_NAME, waitVarName);
        HeapObject heapObject = new HeapObject(new ProgramPoint("clientId", 25), "int", METHOD_NAME, "campId");
        List<EnvEntry> originalEnv = ImmutableList.of(new EnvEntry(waitVarName, waitHeapObject));

        WaitStatement waitStatement = new WaitStatement(35, waitVarName);
        Graph originalGraph = new Graph(ImmutableMap.of(heapObject, Collections.emptySet()));
        Set<HeapObject> originalRoots = ImmutableSet.of(heapObject);
        Set<HeapObject> originalWaits = ImmutableSet.of(heapObject);
        List<HeapObject> originalLocks = ImmutableList.of(
                new HeapObject(
                        new ProgramPoint("some name", 33), "java.lang.String", METHOD_NAME, "new name"),
                waitHeapObject);
        State originalState = new State(
                originalGraph,
                originalRoots,
                originalLocks,
                originalEnv,
                originalWaits);
        //when
        State newState = processor.process(waitStatement, originalState);

        //then
        assertThat(newState.getGraph(), is(originalGraph));
        assertThat(newState.getRoots(), is(originalRoots));
        assertThat(newState.getLocks(), is(originalLocks));
        assertThat(newState.getEnvironment(), is(originalEnv));
        assertThat(newState.getWaits(), is(originalWaits));
    }

    @Test
    @DisplayName("throws an exception when env does not contain mapping for wait variable")
    void process_fail_envIsIncorrect() {
        //given
        WaitStatementProcessor processor = new WaitStatementProcessor();
        String waitVarName = "campaignId";
        HeapObject waitHeapObject = new HeapObject(new ProgramPoint(waitVarName, 23), "java.lang.Long", METHOD_NAME, waitVarName);
        HeapObject heapObject = new HeapObject(new ProgramPoint("clientId", 25), "int", METHOD_NAME, "cmpId");
        List<EnvEntry> originalEnv = ImmutableList.of(new EnvEntry("otherVarName", waitHeapObject));

        WaitStatement waitStatement = new WaitStatement(35, waitVarName);
        Graph originalGraph = new Graph(ImmutableMap.of(heapObject, Collections.emptySet()));
        Set<HeapObject> originalRoots = ImmutableSet.of(heapObject);
        Set<HeapObject> originalWaits = ImmutableSet.of(heapObject);
        List<HeapObject> originalLocks = Collections.emptyList();
        State originalState = new State(
                originalGraph,
                originalRoots,
                originalLocks,
                originalEnv,
                originalWaits);
        //when
        //then
        assertThrows(AlgorithmValidationException.class, () -> processor.process(waitStatement, originalState));
    }

    @Test
    @DisplayName("throws an exception when graph does not contain lock")
    void process_fail_graphIsIncorrect() {
        //given
        WaitStatementProcessor processor = new WaitStatementProcessor();
        String waitVarName = "campaignId";
        HeapObject waitHeapObject = new HeapObject(new ProgramPoint(waitVarName, 23), "java.lang.Long", METHOD_NAME, waitVarName);
        HeapObject heapObject = new HeapObject(new ProgramPoint("clientId", 25), "int", METHOD_NAME, "clId");
        List<EnvEntry> originalEnv = ImmutableList.of(new EnvEntry(waitVarName, waitHeapObject));

        WaitStatement waitStatement = new WaitStatement(35, waitVarName);
        Graph originalGraph = new Graph(ImmutableMap.of(waitHeapObject, Collections.emptySet()));
        Set<HeapObject> originalRoots = ImmutableSet.of(heapObject);
        Set<HeapObject> originalWaits = ImmutableSet.of(heapObject);
        List<HeapObject> originalLocks = ImmutableList.of(
                new HeapObject(
                        new ProgramPoint("some name", 33), "java.lang.String", METHOD_NAME, "newName"),
                heapObject);
        State originalState = new State(
                originalGraph,
                originalRoots,
                originalLocks,
                originalEnv,
                originalWaits);
        //when
        //then
        assertThrows(AlgorithmValidationException.class, () -> processor.process(waitStatement, originalState));
    }
}