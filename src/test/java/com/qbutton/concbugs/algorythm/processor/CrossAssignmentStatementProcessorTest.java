package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import com.qbutton.concbugs.algorythm.service.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrossAssignmentStatementProcessor")
class CrossAssignmentStatementProcessorTest {

    @Mock
    private GraphService graphService;

    private CrossAssignmentStatementProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CrossAssignmentStatementProcessor(graphService);
    }

    @Test
    @DisplayName("processes correctly when there is correct data in original state")
    void process_success() {
        //given
        String varName = "v1";
        String rightVarName = "v2";
        CrossAssignmentStatement statement = new CrossAssignmentStatement(32, varName, rightVarName);

        HeapObject ho1 = new HeapObject(new ProgramPoint(rightVarName, 2), "java.lang.String");
        Set<HeapObject> ho1Set = ImmutableSet.of(ho1);
        List<HeapObject> ho1List = ImmutableList.of(ho1);
        Graph ho1Graph = new Graph(of(ho1, emptySet()));
        EnvEntry ho1EnvEntry = new EnvEntry(rightVarName, ho1);
        State originalState = new State(
                ho1Graph,
                ho1Set,
                ho1List,
                ImmutableList.of(ho1EnvEntry),
                ho1Set
        );
        doCallRealMethod().when(graphService).addOrReplaceEnv(any(), any());

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
        assertThat(newEnv.get(1), is(new EnvEntry(varName, ho1)));
    }

    @Test
    @DisplayName("fails with exception when there is no env entry for given rightVarName in original state")
    void process_failure_stateIsIncorrect() {
        //given
        String varName = "v1";
        String rightVarName = "v2";
        CrossAssignmentStatement statement = new CrossAssignmentStatement(32, varName, rightVarName);

        HeapObject ho1 = new HeapObject(new ProgramPoint(rightVarName, 2), "java.lang.String");
        Set<HeapObject> ho1Set = ImmutableSet.of(ho1);
        List<HeapObject> ho1List = ImmutableList.of(ho1);
        Graph ho1Graph = new Graph(of(ho1, emptySet()));
        EnvEntry ho1EnvEntry = new EnvEntry("vv2", ho1);
        State originalState = new State(
                ho1Graph,
                ho1Set,
                ho1List,
                ImmutableList.of(ho1EnvEntry),
                ho1Set
        );

        //when
        //then
        assertThrows(AlgorithmValidationException.class, () -> processor.process(statement, originalState));
    }
}