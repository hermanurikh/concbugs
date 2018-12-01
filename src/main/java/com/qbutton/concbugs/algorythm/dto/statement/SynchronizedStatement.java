package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * Synchronized statement, e.g.:
 * synchronized(v).
 */
public final class SynchronizedStatement extends Statement {
    public SynchronizedStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
