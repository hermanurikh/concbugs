package com.qbutton.concbugs.algorythm.dto.statement;

import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import lombok.Getter;

import java.util.List;

/**
 * Method statement, e.g.:
 * v = method(v1, ... vn).
 */
@Getter
public final class MethodStatement extends Statement {
    //todo where searching of all method declarations will be implemented?
    private final List<MethodDeclaration> methodDeclarations;
    private final Class<?> returnType;

    public MethodStatement(int lineNumber, String varName, List<MethodDeclaration> methodDeclarations, Class<?> returnType) {
        super(lineNumber, varName);
        this.methodDeclarations = methodDeclarations;
        this.returnType = returnType;
    }
}
