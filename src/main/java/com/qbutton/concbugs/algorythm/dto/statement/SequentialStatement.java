package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;

/**
 * Sequential statement (to be executed sequentially), e.g.:
 * statement1; statement2.
 */
@Getter
public final class SequentialStatement extends Statement {
    private final Statement stmt1;
    private final Statement stmt2;

    public SequentialStatement(int lineNumber, String varName, Statement stmt1, Statement stmt2) {
        super(lineNumber, varName);
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
    }
}
