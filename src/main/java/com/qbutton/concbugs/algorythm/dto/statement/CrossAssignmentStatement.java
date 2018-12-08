package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;

/**
 * CrossAssignment statement, e.g.:
 * v1 = v2.
 */
@Getter
public final class CrossAssignmentStatement extends Statement {
    private final String rightValueName;

    public CrossAssignmentStatement(int lineNumber, String leftVarName, String rightValueName) {
        super(lineNumber, leftVarName);
        this.rightValueName = rightValueName;
    }
}
