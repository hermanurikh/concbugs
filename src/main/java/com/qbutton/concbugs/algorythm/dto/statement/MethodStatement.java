package com.qbutton.concbugs.algorythm.dto.statement;

import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Method statement, e.g.:
 * v = method(v1, ... vn).
 */
@Getter
@ToString(callSuper = true)
public final class MethodStatement extends Statement {
    //todo where searching of all method declarations will be implemented?
    private final List<MethodDeclaration> methodDeclarations;
    private final String returnType;

    public MethodStatement(int lineNumber, String varName, List<MethodDeclaration> methodDeclarations, String returnType) {
        super(lineNumber, varName);
        this.methodDeclarations = methodDeclarations;
        this.returnType = returnType;
    }
}
