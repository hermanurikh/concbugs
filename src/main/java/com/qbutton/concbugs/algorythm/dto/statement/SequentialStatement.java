package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * Sequential statement (to be executed sequentially), e.g.:
 * statement1; statement2.
 */
public final class SequentialStatement extends Statement {
    public SequentialStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
