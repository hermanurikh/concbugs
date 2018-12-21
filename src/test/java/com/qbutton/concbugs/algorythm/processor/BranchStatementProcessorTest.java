package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.service.MergeService;
import com.qbutton.concbugs.algorythm.service.VisitorService;
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
@DisplayName("BranchStatementProcessor")
class BranchStatementProcessorTest {

    @Mock
    private VisitorService visitorService;
    @Mock
    private MergeService mergeService;

    private BranchStatementProcessor branchStatementProcessor;

    @BeforeEach
    void init() {
        branchStatementProcessor = new BranchStatementProcessor(visitorService, mergeService);
    }

    @Test
    @DisplayName("processes correctly when statements are not null")
    void process() {
        //given
        int lineNumber = 32;
        Statement statement1 = new WaitStatement(lineNumber, "1");
        Statement statement2 = new WaitStatement(lineNumber, "2");

        State initialState = Mockito.mock(State.class);
        State state2 = Mockito.mock(State.class);
        State state3 = Mockito.mock(State.class);
        State resultState = Mockito.mock(State.class);

        when(visitorService.visitStatement(any(), any())).thenAnswer(invocationOnMock -> {
            WaitStatement statement = (WaitStatement) invocationOnMock.getArguments()[0];
            if (statement.getVarName().equals("1")
                    && invocationOnMock.getArguments()[1] == initialState) {
                return state2;
            }
            if (statement.getVarName().equals("2")
                    && invocationOnMock.getArguments()[1] == initialState) {
                return state3;
            }
            return null;
        });

        when(mergeService.mergeStates(state2, state3, 34)).thenReturn(resultState);

        //when
        State actual = branchStatementProcessor.process(
                new BranchStatement(34, "", statement1, statement2), initialState);

        //then
        assertThat(actual, is(resultState));

        verify(visitorService).visitStatement(eq(new WaitStatement(lineNumber, "1")), eq(initialState));
        verify(visitorService).visitStatement(eq(new WaitStatement(lineNumber, "2")), eq(initialState));
        verifyNoMoreInteractions(visitorService);

        verify(mergeService).mergeStates(state2, state3, 34);
        verifyNoMoreInteractions(mergeService);
    }

    @Test
    @DisplayName("processes correctly when first statement is null")
    void process_firstStatementIsNull() {
        //given
        int lineNumber = 32;
        Statement statement2 = new WaitStatement(lineNumber, "2");

        State initialState = Mockito.mock(State.class);
        State state3 = Mockito.mock(State.class);
        State resultState = Mockito.mock(State.class);

        when(visitorService.visitStatement(statement2, initialState)).thenReturn(state3);
        when(mergeService.mergeStates(State.EMPTY_STATE, state3, 34)).thenReturn(resultState);

        //when
        State actual = branchStatementProcessor.process(
                new BranchStatement(34, "", null, statement2), initialState);

        //then
        assertThat(actual, is(resultState));

        verify(visitorService).visitStatement(eq(new WaitStatement(lineNumber, "2")), eq(initialState));
        verifyNoMoreInteractions(visitorService);

        verify(mergeService).mergeStates(State.EMPTY_STATE, state3, 34);
        verifyNoMoreInteractions(mergeService);
    }

    @Test
    @DisplayName("processes correctly when second statement is null")
    void process_secondStatementIsNull() {
        //given
        int lineNumber = 32;
        Statement statement2 = new WaitStatement(lineNumber, "2");

        State initialState = Mockito.mock(State.class);
        State state3 = Mockito.mock(State.class);
        State resultState = Mockito.mock(State.class);

        when(visitorService.visitStatement(statement2, initialState)).thenReturn(state3);
        when(mergeService.mergeStates(state3, State.EMPTY_STATE, 66)).thenReturn(resultState);

        //when
        State actual = branchStatementProcessor.process(
                new BranchStatement(66, "", statement2, null), initialState);

        //then
        assertThat(actual, is(resultState));

        verify(visitorService).visitStatement(eq(new WaitStatement(lineNumber, "2")), eq(initialState));
        verifyNoMoreInteractions(visitorService);

        verify(mergeService).mergeStates(state3, State.EMPTY_STATE, 66);
        verifyNoMoreInteractions(mergeService);
    }

    @Test
    @DisplayName("processes correctly when both statements are null")
    void process_bothStatementsAreNull() {
        //given
        State initialState = Mockito.mock(State.class);

        //when
        State actual = branchStatementProcessor.process(
                new BranchStatement(66, "", null, null), initialState);

        //then
        assertThat(actual, is(initialState));

        verifyNoMoreInteractions(visitorService);
        verifyNoMoreInteractions(mergeService);
    }
}