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

    public SynchronizedStatement(int offset, String varName, Statement innerStatement, String className) {
        super(offset, varName);
        this.innerStatement = innerStatement;
        this.className = className;
    }
}
