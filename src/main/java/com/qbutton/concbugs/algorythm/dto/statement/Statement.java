package com.qbutton.concbugs.algorythm.dto.statement;

import lombok.Data;

@Data
public abstract class Statement {
    protected final int offset;
    protected final String varName;
}
