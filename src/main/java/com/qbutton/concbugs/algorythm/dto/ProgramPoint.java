package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * Program point - a combination of variable name and line number.
 */
@Data
public final class ProgramPoint implements Cloneable {
    private final String variableName;
    private final int offset;

    public static final ProgramPoint UNKNOWN = new ProgramPoint(null, -1);

    public boolean isUnknown() {
        return this.equals(UNKNOWN);
    }

    @SneakyThrows
    @Override
    public ProgramPoint clone() {
        return (ProgramPoint) super.clone();
    }
}
