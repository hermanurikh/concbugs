package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Data;

@Data
public abstract class Statement {
    protected final int lineNumber;
    protected final String varName;
}
