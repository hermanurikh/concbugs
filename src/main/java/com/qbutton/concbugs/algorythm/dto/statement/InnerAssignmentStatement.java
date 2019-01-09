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
    private final String enclosingMethodName;

    public InnerAssignmentStatement(int offset, String varName, String clazz, String enclosingMethodName) {
        super(offset, varName);
        this.clazz = clazz;
        this.enclosingMethodName = enclosingMethodName;
    }
}
