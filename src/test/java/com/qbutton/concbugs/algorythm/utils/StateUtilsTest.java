package com.qbutton.concbugs.algorythm.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateUtils class")
class StateUtilsTest {

    @Nested
    @DisplayName("merges graphs and roots when merging states")
    class MergeGraphsAndRoots {

        @Test
        @DisplayName("correctly when graph has one object, parameter is formal and actual is among locks")
        void oneObject_formalAndActual_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            Graph calleeGraph = new Graph(ImmutableMap.of(ho1, emptySet()));
            Set<HeapObject> calleeRoots = ImmutableSet.of(ho1);
            List<HeapObject> calleeLocks = emptyList();
            List<EnvEntry> calleeEnv = ImmutableList.of(new EnvEntry("my var", ho1));
            Set<HeapObject> calleeWaits = emptySet();

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            HeapObject ho2 = new HeapObject(new ProgramPoint("my initial var", 12), Integer.class);
            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = ImmutableList.of(ho2);
            List<EnvEntry> callerEnv = ImmutableList.of(new EnvEntry("my initial var", ho2));
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            assertThat(state.getGraph().getNeighbors().size(), is(0));
            assertThat(state.getRoots().size(), is(0));
            assertThat(state.getLocks().size(), is(0));
            assertThat(state.getWaits().size(), is(0));
            assertThat(state.getEnvironment(), is(calleeEnv));
        }

        @Test
        @DisplayName("correctly when graph has one object, parameter is formal, but actual is not among locks")
        void oneObject_onlyFormal_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            Graph calleeGraph = new Graph(ImmutableMap.of(ho1, emptySet()));
            Set<HeapObject> calleeRoots = ImmutableSet.of(ho1);
            List<HeapObject> calleeLocks = emptyList();
            List<EnvEntry> calleeEnv = ImmutableList.of(new EnvEntry("my var", ho1));
            Set<HeapObject> calleeWaits = emptySet();

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            HeapObject ho2 = new HeapObject(new ProgramPoint("my initial var", 12), Integer.class);
            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = ImmutableList.of();
            List<EnvEntry> callerEnv = ImmutableList.of(new EnvEntry("my initial var", ho2));
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            assertThat(state.getGraph().getNeighbors().size(), is(1));
            assertTrue(state.getGraph().getNeighbors().containsKey(ho2));
            assertThat(state.getRoots().size(), is(1));
            assertTrue(state.getRoots().contains(ho2));
            assertThat(state.getLocks().size(), is(0));
            assertThat(state.getWaits().size(), is(0));
            assertThat(state.getEnvironment(), is(calleeEnv));
        }

        @Test
        @DisplayName("correctly when graph has one object which is not among formal params")
        void oneObject_notFormal_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            Graph calleeGraph = new Graph(ImmutableMap.of(ho1, emptySet()));
            Set<HeapObject> calleeRoots = ImmutableSet.of(ho1);
            List<HeapObject> calleeLocks = emptyList();
            List<EnvEntry> calleeEnv = emptyList();
            Set<HeapObject> calleeWaits = emptySet();

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = ImmutableList.of();
            List<EnvEntry> callerEnv = emptyList();
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            HeapObject expected = new HeapObject(ProgramPoint.UNKNOWN, Integer.class);
            assertThat(state.getGraph().getNeighbors().size(), is(1));
            assertTrue(state.getGraph().getNeighbors().containsKey(expected));
            assertThat(state.getRoots().size(), is(1));
            assertTrue(state.getRoots().contains(expected));
            assertThat(state.getLocks().size(), is(0));
            assertThat(state.getWaits().size(), is(0));
            assertThat(state.getEnvironment(), is(calleeEnv));
        }

        @Test
        @DisplayName("correctly when there are 4 method params, 3 of which in graph, each with different scenario")
        void fourObjects_differentScenarios_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            HeapObject ho2 = new HeapObject(new ProgramPoint("my var2", 25), String.class);
            HeapObject ho3 = new HeapObject(new ProgramPoint("my var3", 26), Number.class);
            HeapObject ho4 = new HeapObject(new ProgramPoint("my var4", 27), Map.class);
            Graph calleeGraph = new Graph(ImmutableMap.of(
                    ho1, ImmutableSet.of(ho2, ho3),
                    ho2, ImmutableSet.of(ho3),
                    ho3, ImmutableSet.of(ho2)));
            List<EnvEntry> calleeEnv = ImmutableList.of(
                    new EnvEntry("my var4", ho4),
                    new EnvEntry("my var", ho1),
                    new EnvEntry("my var2", ho2));
            Set<HeapObject> calleeRoots = ImmutableSet.of(ho1, ho2);
            List<HeapObject> calleeLocks = emptyList();
            Set<HeapObject> calleeWaits = emptySet();

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            HeapObject actualHo4 = new HeapObject(new ProgramPoint("my initial var4", 12), Map.class);
            HeapObject actualHo1 = new HeapObject(new ProgramPoint("my initial var", 12), Integer.class);
            HeapObject actualHo2 = new HeapObject(new ProgramPoint("my initial var2", 12), String.class);
            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = ImmutableList.of(actualHo1);
            List<EnvEntry> callerEnv = ImmutableList.of(
                    new EnvEntry("my initial var4", actualHo4),
                    new EnvEntry("my initial var", actualHo1),
                    new EnvEntry("my initial var2", actualHo2));
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            /*
                ho1 has been removed from graph
                ho2 has been replaced by actualHo2
                ho3 has been replaced by unknown heap object
             */
            HeapObject expectedUnknownObject = new HeapObject(ProgramPoint.UNKNOWN, Number.class);
            Map<HeapObject, Set<HeapObject>> newGraph = state.getGraph().getNeighbors();
            assertThat(newGraph.size(), is(2));
            assertTrue(newGraph.containsKey(actualHo2));
            assertThat(newGraph.get(actualHo2).size(), is(1));
            assertThat(newGraph.get(actualHo2).iterator().next(), is(expectedUnknownObject));
            assertTrue(newGraph.containsKey(expectedUnknownObject));
            assertThat(newGraph.get(expectedUnknownObject).size(), is(1));
            assertThat(newGraph.get(expectedUnknownObject).iterator().next(), is(actualHo2));
            assertThat(state.getLocks().size(), is(0));
            assertThat(state.getWaits().size(), is(0));
            assertThat(state.getEnvironment(), is(calleeEnv));
            assertThat(state.getRoots().size(), is(2));
            assertTrue(state.getRoots().contains(actualHo2));
            assertTrue(state.getRoots().contains(expectedUnknownObject));
        }
    }

    @Nested
    @DisplayName("merges waits when merging states")
    class MergeWaits {

        @Test
        @DisplayName("correctly when there is one wait in formal parameters")
        void oneWait_formal_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            Graph calleeGraph = new Graph(emptyMap());
            List<EnvEntry> calleeEnv = ImmutableList.of(new EnvEntry("my var", ho1));
            Set<HeapObject> calleeRoots = emptySet();
            List<HeapObject> calleeLocks = emptyList();
            Set<HeapObject> calleeWaits = ImmutableSet.of(ho1);

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            HeapObject actualHo1 = new HeapObject(new ProgramPoint("my initial var", 12), Integer.class);
            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = emptyList();
            List<EnvEntry> callerEnv = ImmutableList.of(new EnvEntry("my initial var", actualHo1));
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            assertThat(state.getWaits().size(), is(1));
            assertTrue(state.getWaits().contains(actualHo1));
        }

        @Test
        @DisplayName("correctly when there is one wait not in formal parameters")
        void oneWait_inFormal_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            Graph calleeGraph = new Graph(emptyMap());
            List<EnvEntry> calleeEnv = emptyList();
            Set<HeapObject> calleeRoots = emptySet();
            List<HeapObject> calleeLocks = emptyList();
            Set<HeapObject> calleeWaits = ImmutableSet.of(ho1);

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = emptyList();
            List<EnvEntry> callerEnv = emptyList();
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            assertThat(state.getWaits().size(), is(1));
            assertTrue(state.getWaits().contains(new HeapObject(ProgramPoint.UNKNOWN, Integer.class)));
        }

        @Test
        @DisplayName("correctly when there are 2 waits with different scenarios")
        void twoWaits_differentScenarios_success() {
            //given
            HeapObject ho1 = new HeapObject(new ProgramPoint("my var", 24), Integer.class);
            HeapObject ho2 = new HeapObject(new ProgramPoint("my var2", 25), Number.class);
            Graph calleeGraph = new Graph(emptyMap());
            List<EnvEntry> calleeEnv = ImmutableList.of(new EnvEntry("my var2", ho2));
            Set<HeapObject> calleeRoots = emptySet();
            List<HeapObject> calleeLocks = emptyList();
            Set<HeapObject> calleeWaits = ImmutableSet.of(ho1, ho2);

            State returnedMethodState = new State(calleeGraph, calleeRoots, calleeLocks, calleeEnv, calleeWaits);

            HeapObject actualHo2 = new HeapObject(new ProgramPoint("my initial var2", 12), Number.class);
            Graph callerGraph = new Graph(emptyMap());
            Set<HeapObject> callerRoots = emptySet();
            List<HeapObject> callerLocks = emptyList();
            List<EnvEntry> callerEnv = ImmutableList.of(new EnvEntry("my initial var2", actualHo2));
            Set<HeapObject> callerWaits = emptySet();

            State currentState = new State(callerGraph, callerRoots, callerLocks, callerEnv, callerWaits);

            //when
            State state = StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState);

            //then
            assertThat(state.getWaits().size(), is(2));
            assertTrue(state.getWaits().contains(actualHo2));
            assertTrue(state.getWaits().contains(new HeapObject(ProgramPoint.UNKNOWN, Integer.class)));
        }
    }

    @Test
    @DisplayName("fails to merge when formal and actual parameters length differ")
    void renameFromCalleeToCallerContext_fail_parametersLenghDiffer() {
        //given
        List<EnvEntry> calleeEnv = ImmutableList.of(
                new EnvEntry("my var2", new HeapObject(ProgramPoint.UNKNOWN, Integer.class)));
        State returnedMethodState = new State(
                new Graph(emptyMap()), emptySet(), emptyList(), calleeEnv, emptySet());

        List<EnvEntry> callerEnv = emptyList();
        State currentState = new State(
                new Graph(emptyMap()), emptySet(), emptyList(), callerEnv, emptySet());

        //when
        //then
        assertThrows(AlgorithmValidationException.class,
                () -> StateUtils.renameFromCalleeToCallerContext(returnedMethodState, currentState));
    }
}