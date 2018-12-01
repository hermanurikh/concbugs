package com.qbutton.concbugs.algorythm.dto.statement;

/**
 * CrossAssignment statement, e.g.:
 * v1 = v2.
 */
public final class CrossAssignmentStatement extends Statement {
    public CrossAssignmentStatement(int lineNumber, String varName) {
        super(lineNumber, varName);
    }
}
