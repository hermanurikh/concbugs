package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;
import lombok.ToString;

/**
 * Declaration statement, e.g.:
 * T v
 * or
 * v = new T().
 */
@Getter
@ToString(callSuper = true)
public final class DeclarationStatement extends Statement {
    private final String clazz;
    private final String enclosingMethodName;

    public DeclarationStatement(int offset, String varName, String clazz, String enclosingMethodName) {
        super(offset, varName);
        this.clazz = clazz;
        this.enclosingMethodName = enclosingMethodName;
    }
}
