package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;

/**
 * Declaration statement, e.g.:
 * T v
 * or
 * v = new T().
 */
@Getter
public final class DeclarationStatement extends Statement {
    private final String clazz;

    public DeclarationStatement(int lineNumber, String varName, String clazz) {
        super(lineNumber, varName);
        this.clazz = clazz;
    }
}
