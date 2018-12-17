package com.qbutton.concbugs.algorythm.dto;

import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import lombok.Data;

import java.util.List;

@Data
public class MethodDeclaration {
    private final String methodName;
    /**
     * v1 in list of variables is 'this' if it is an instance method.
     */
    private final List<Variable> variables;
    private final Statement methodBody;

    @Data
    public static class Variable {
        private final String variableName;
        private final Class<?> variableClass;
    }
}
