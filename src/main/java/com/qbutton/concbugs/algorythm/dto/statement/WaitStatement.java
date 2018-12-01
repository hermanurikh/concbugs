package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * Wait statement, e.g.:
 * v.wait().
 */
public final class WaitStatement extends Statement {
    public WaitStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
