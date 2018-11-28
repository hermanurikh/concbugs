package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

/**
 * Program point - a combination of variable name and line number.
 */
@Data
public class ProgramPoint {
    private final String variableName;
    private final int lineNumber;

    private static final ProgramPoint UNKNOWN = new ProgramPoint(null, -1);

    private boolean isUnknown() {
        return this == UNKNOWN;
    }
}
