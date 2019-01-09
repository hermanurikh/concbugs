package com.qbutton.concbugs.algorythm;

import com.google.common.collect.ImmutableList;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.VisualisationNode;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.processor.ProcessorFacade;
import com.qbutton.concbugs.algorythm.service.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlgorythmFacade")
class AlgorythmFacadeTest {

    @Mock
    private ProcessorFacade processorFacade;
    @Mock
    private GraphService graphService;

    private AlgorythmFacade algorythmFacade;

    @BeforeEach
    void init() {
        algorythmFacade = new AlgorythmFacade(processorFacade, graphService);
    }

    @Test
    void visitLibrary() {
        //given
        MethodStatement method1 = new MethodStatement(0, "0", emptyList(), "int", emptyList());
        MethodStatement method2 = new MethodStatement(1, "1", emptyList(), "java.lang.String", emptyList());

        State methodProcessResult1 = Mockito.mock(State.class);
        State methodProcessResult2 = Mockito.mock(State.class);

        when(processorFacade.process(any(), eq(State.EMPTY_STATE))).thenAnswer(invocation -> {
            MethodStatement methodStatement = (MethodStatement) invocation.getArguments()[0];
            if (methodStatement.equals(method1)) {
                return methodProcessResult1;
            }

            if (methodStatement.equals(method2)) {
                return methodProcessResult2;
            }

            return null;
        });

        Map<VisualisationNode, Set<VisualisationNode>> emptyGraph = Collections.emptyMap();
        when(graphService.postProcess(ImmutableList.of(methodProcessResult1, methodProcessResult2)))
                .thenReturn(emptyGraph);

        //when
        Map<VisualisationNode, Set<VisualisationNode>> resultGraph = algorythmFacade.visitLibrary(ImmutableList.of(method1, method2));

        //then
        assertThat(resultGraph, is(emptyGraph));

        verify(processorFacade).process(method1, State.EMPTY_STATE);
        verify(processorFacade).process(method2, State.EMPTY_STATE);
        verifyNoMoreInteractions(processorFacade);

        verify(graphService).postProcess(ImmutableList.of(methodProcessResult1, methodProcessResult2));
    }
}