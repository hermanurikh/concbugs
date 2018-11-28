package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * Program point - a combination of variable name and line number.
 */
@Data
public class ProgramPoint implements Cloneable {
    private final String variableName;
    private final int lineNumber;

    private static final ProgramPoint UNKNOWN = new ProgramPoint(null, -1);

    private boolean isUnknown() {
        return this == UNKNOWN;
    }

    @SneakyThrows
    @Override
    public ProgramPoint clone() {
        return (ProgramPoint) super.clone();
    }
}
