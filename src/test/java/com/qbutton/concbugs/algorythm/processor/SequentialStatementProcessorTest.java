package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SequentialStatementProcessor")
class SequentialStatementProcessorTest {

    @Mock
    private ProcessorFacade processorFacade;

    private SequentialStatementProcessor sequentialStatementProcessor;

    @BeforeEach
    void init() {
        sequentialStatementProcessor = new SequentialStatementProcessor(processorFacade);
    }

    @Test
    @DisplayName("processes correctly")
    void process() {
        //given
        int lineNumber = 32;
        Statement statement1 = new WaitStatement(lineNumber, "first");
        Statement statement2 = new WaitStatement(lineNumber, "second");

        State state1 = Mockito.mock(State.class);
        State state2 = Mockito.mock(State.class);
        State state3 = Mockito.mock(State.class);

        when(processorFacade.process(any(), any())).thenAnswer(invocationOnMock -> {
            WaitStatement statement = (WaitStatement) invocationOnMock.getArguments()[0];
            if (statement.getVarName().equals("first")
                    && invocationOnMock.getArguments()[1] == state1) {
                return state2;
            }
            if (statement.getVarName().equals("second")
                    && invocationOnMock.getArguments()[1] == state2) {
                return state3;
            }
            return null;
        });

        //when
        State resultState = sequentialStatementProcessor.process(
                new SequentialStatement(35, "", statement1, statement2), state1);

        //then
        assertThat(resultState, is(state3));
        verify(processorFacade).process(eq(new WaitStatement(lineNumber, "first")), eq(state1));
        verify(processorFacade).process(eq(new WaitStatement(lineNumber, "second")), eq(state2));
        verifyNoMoreInteractions(processorFacade);
    }
}