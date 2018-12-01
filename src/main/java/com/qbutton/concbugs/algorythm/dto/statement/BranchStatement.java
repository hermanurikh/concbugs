package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * Branch statement, e.g.:
 * if (...) statement1 else statement2.
 */
public final class BranchStatement extends Statement {
    public BranchStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
