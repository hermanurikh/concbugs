package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * Declaration statement, e.g.:
 * T v
 * or
 * v = new T().
 */
public final class DeclarationStatement extends Statement {
    public DeclarationStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
