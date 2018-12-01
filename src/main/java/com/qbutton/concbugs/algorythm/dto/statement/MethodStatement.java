package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * Method statement, e.g.:
 * v = method(v1, ... vn).
 */
public final class MethodStatement extends Statement {
    public MethodStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
