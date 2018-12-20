package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;
import lombok.ToString;

/**
 * CrossAssignment statement, e.g.:
 * v1 = v2.
 */
@Getter
@ToString(callSuper = true)
public final class CrossAssignmentStatement extends Statement {
    private final String rightValueName;

    public CrossAssignmentStatement(int lineNumber, String leftVarName, String rightValueName) {
        super(lineNumber, leftVarName);
        this.rightValueName = rightValueName;
    }
}
