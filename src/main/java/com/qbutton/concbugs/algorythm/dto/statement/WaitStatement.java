package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.ToString;

/**
 * Wait statement, e.g.:
 * v.wait().
 */
@ToString(callSuper = true)
public final class WaitStatement extends Statement {
    public WaitStatement(int offset, String varName) {
        super(offset, varName);
    }
}
