package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;
import lombok.ToString;

/**
 * Synchronized statement, e.g.:
 * synchronized(v).
 */
@Getter
@ToString(callSuper = true)
public final class SynchronizedStatement extends Statement {
    private final Statement innerStatement;

    public SynchronizedStatement(int lineNumber, String varName, Statement innerStatement) {
        super(lineNumber, varName);
        this.innerStatement = innerStatement;
    }
}
