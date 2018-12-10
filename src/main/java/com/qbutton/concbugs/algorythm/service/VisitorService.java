package com.qbutton.concbugs.algorythm.service;

import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.processor.ProcessorFacade;
import lombok.RequiredArgsConstructor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

@RequiredArgsConstructor
public final class VisitorService {

    private final ProcessorFacade processorFacade;

    public State visitStatement(Statement statement, State state) {
        return processorFacade.process(statement, state);
    }

    public State visitMethod(MethodDeclaration methodDeclaration) {
        State newState = new State(new Graph(
                emptyMap()),
                emptySet(),
                emptyList(),
                emptyList(),
                emptySet()
        );

        //process formals via "T v" rule
        for (MethodDeclaration.Variable var : methodDeclaration.getVariables()) {
            newState = visitStatement(new DeclarationStatement(
                    methodDeclaration.getMethodBody().getLineNumber(),
                    var.getVariableName(),
                    var.getVariableClass()
            ), newState);
        }

        newState = visitStatement(methodDeclaration.getMethodBody(), newState);

        return newState;
    }
}