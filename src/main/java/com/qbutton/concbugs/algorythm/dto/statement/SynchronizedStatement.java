package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Synchronized statement, e.g.:
 * synchronized(v).
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class SynchronizedStatement extends Statement {
    private final Statement innerStatement;
    private final String className;
    private final String enclosingMethodName;

    public SynchronizedStatement(int offset, String varName, Statement innerStatement, String className,
                                 String enclosingMethodName) {
        super(offset, varName);
        this.innerStatement = innerStatement;
        this.className = className;
        this.enclosingMethodName = enclosingMethodName;
    }
}
