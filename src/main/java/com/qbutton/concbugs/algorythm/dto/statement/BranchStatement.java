package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;

/**
 * Branch statement, e.g.:
 * if (...) statement1 else statement2.
 */
@Getter
public final class BranchStatement extends Statement {
    private final Statement stmt1;
    private final Statement stmt2;

    public BranchStatement(int lineNumber, String varName, Statement stmt1, Statement stmt2) {
        super(lineNumber, varName);
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
    }
}
