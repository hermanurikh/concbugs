package com.qbutton.concbugs.algorythm.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

/**
 * Heap object - a combination of program point and variable class.
 */
@Data
public final class HeapObject implements Cloneable {
    private final ProgramPoint programPoint;
    private final String clazz;

    @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
    @Override
    public HeapObject clone() {
        ProgramPoint programPointClone = this.programPoint.clone();
        return new HeapObject(programPointClone, clazz);
    }
}
