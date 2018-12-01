package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * InnerAssignment statement, e.g.:
 * v1 = v2.field.
 */
public final class InnerAssignmentStatement extends Statement {
    public InnerAssignmentStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
