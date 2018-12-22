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
    private final List<MethodDeclaration> methodDeclarations;
    private final String returnType;
    private final List<String> actualParameters;

    public MethodStatement(int offset, String varName, List<MethodDeclaration> methodDeclarations, String returnType, List<String> actualParameters) {
        super(offset, varName);
        this.methodDeclarations = methodDeclarations;
        this.returnType = returnType;
        this.actualParameters = actualParameters;
    }
}
