package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.ImmutableList;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.processor.ProcessorFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorService")
class VisitorServiceTest {
    @Mock
    private ProcessorFacade processorFacade;

    private VisitorService visitorService;

    @BeforeEach
    void init() {
        visitorService = new VisitorService();
        visitorService.setProcessorFacade(processorFacade);
    }

    @Test
    @DisplayName("visits statement correctly")
    void visitStatement() {
        //given
        Statement statement = new WaitStatement(32, "123");
        State state = Mockito.mock(State.class);

        //when
        visitorService.visitStatement(statement, state);

        //then
        verify(processorFacade).process(eq(statement), eq(state));
    }

    @Test
    @DisplayName("visits method correctly")
    void visitMethod() {
        //given
        int offset = 34;
        String varName = "some var";
        Statement methodBody = new DeclarationStatement(offset, varName, "java.lang.Double", "myMethod");
        State emptyState = new State(
                new Graph(
                        emptyMap()
                ),
                emptySet(),
                emptyList(),
                emptyList(),
                emptySet());
        State state2 = Mockito.mock(State.class);
        State state3 = Mockito.mock(State.class);
        State state4 = Mockito.mock(State.class);
        MethodDeclaration methodDeclaration = new MethodDeclaration(
                "foo",
                ImmutableList.of(
                        new MethodDeclaration.Variable("a", "int"),
                        new MethodDeclaration.Variable("b", "java.lang.String")),
                methodBody,
                34);
        when(processorFacade.process(any(), any())).thenAnswer(invocationOnMock -> {
            DeclarationStatement statement = (DeclarationStatement) invocationOnMock.getArguments()[0];
            if (statement.getVarName().equals("a")
                    && Objects.equals(statement.getClazz(), "int")
                    && invocationOnMock.getArguments()[1].equals(emptyState)) {
                return state2;
            }
            if (statement.getVarName().equals("b")
                    && Objects.equals(statement.getClazz(), "java.lang.String")
                    && invocationOnMock.getArguments()[1] == state2) {
                return state3;
            }
            if (statement.getVarName().equals(varName)
                    && Objects.equals(statement.getClazz(), "java.lang.Double")
                    && invocationOnMock.getArguments()[1] == state3) {
                return state4;
            }
            return null;
        });

        //when
        State newState = visitorService.visitMethod(methodDeclaration);

        //then
        assertThat(newState, is(state4));
        verify(processorFacade).process(eq(new DeclarationStatement(offset, "a", "int", "myMethod")), eq(emptyState));
        verify(processorFacade).process(eq(new DeclarationStatement(offset, "b", "java.lang.String", "myMethod")), eq(state2));
        verify(processorFacade).process(eq(methodBody), eq(state3));
        verifyNoMoreInteractions(processorFacade);
    }
}