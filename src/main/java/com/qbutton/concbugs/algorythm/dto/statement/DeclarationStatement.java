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
    private final Class<?> clazz;

    public DeclarationStatement(int lineNumber, String varName, Class<?> clazz) {
        super(lineNumber, varName);
        this.clazz = clazz;
    }
}
