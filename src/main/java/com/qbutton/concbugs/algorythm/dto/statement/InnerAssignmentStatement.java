package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Getter;
import lombok.ToString;

/**
 * InnerAssignment statement, e.g.:
 * v1 = v2.field.
 */
@Getter
@ToString(callSuper = true)
public final class InnerAssignmentStatement extends Statement {
    private final String clazz;

    public InnerAssignmentStatement(int lineNumber, String varName, String clazz) {
        super(lineNumber, varName);
        this.clazz = clazz;
    }
}
