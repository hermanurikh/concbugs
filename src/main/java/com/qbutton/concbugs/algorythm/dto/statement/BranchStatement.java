package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;
import lombok.ToString;

/**
 * Branch statement, e.g.:
 * if (...) statement1 else statement2.
 */
@Getter
@ToString(callSuper = true)
public final class BranchStatement extends Statement {
    private final Statement stmt1;
    private final Statement stmt2;
    private final String enclosingMethodName;

    public BranchStatement(int offset, String varName, Statement stmt1, Statement stmt2, String enclosingMethodName) {
        super(offset, varName);
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
        this.enclosingMethodName = enclosingMethodName;
    }
}
