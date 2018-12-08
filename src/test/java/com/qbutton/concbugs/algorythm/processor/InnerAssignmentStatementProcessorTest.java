package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@DisplayName("InnerAssignmentStatementProcessor")
class InnerAssignmentStatementProcessorTest {

    @Test
    @DisplayName("processes correctly when there is something in original state")
    void process_success() {
        //given
        InnerAssignmentStatementProcessor processor = new InnerAssignmentStatementProcessor();
        String varName = "v1";
        InnerAssignmentStatement statement = new InnerAssignmentStatement(32, varName, Integer.class);

        HeapObject ho1 = new HeapObject(new ProgramPoint("3", 2), String.class);
        Set<HeapObject> ho1Set = ImmutableSet.of(ho1);
        List<HeapObject> ho1List = ImmutableList.of(ho1);
        Graph ho1Graph = new Graph(of(ho1, emptySet()));
        EnvEntry ho1EnvEntry = new EnvEntry("3", ho1);
        State originalState = new State(
                ho1Graph,
                ho1Set,
                ho1List,
                ImmutableList.of(ho1EnvEntry),
                ho1Set
        );

        //when
        State newState = processor.process(statement, originalState);

        //then
        assertThat(newState.getGraph(), is(ho1Graph));
        assertThat(newState.getRoots(), is(ho1Set));
        assertThat(newState.getLocks(), is(ho1List));
        assertThat(newState.getWaits(), is(ho1Set));
        List<EnvEntry> newEnv = newState.getEnvironment();
        assertThat(newEnv.size(), is(2));
        assertThat(newEnv.get(0), is(ho1EnvEntry));
        assertThat(newEnv.get(1),
                is(new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class))));
    }

    @Test
    @DisplayName("processes correctly when original state is empty")
    void process_emptyState_success() {
        //given
        InnerAssignmentStatementProcessor processor = new InnerAssignmentStatementProcessor();
        String varName = "v1";
        InnerAssignmentStatement statement = new InnerAssignmentStatement(32, varName, Integer.class);


        Graph graph = new Graph(Collections.emptyMap());
        State originalState = new State(
                graph,
                emptySet(),
                emptyList(),
                emptyList(),
                emptySet()
        );

        //when
        State newState = processor.process(statement, originalState);

        //then
        assertThat(newState.getGraph(), is(graph));
        assertThat(newState.getRoots(), is(emptySet()));
        assertThat(newState.getLocks(), is(emptyList()));
        assertThat(newState.getWaits(), is(emptySet()));
        List<EnvEntry> newEnv = newState.getEnvironment();
        assertThat(newEnv.size(), is(1));
        assertThat(newEnv.get(0),
                is(new EnvEntry(varName, new HeapObject(new ProgramPoint(varName, 32), Integer.class))));
    }
}