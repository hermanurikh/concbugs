package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

/**
 * Heap object - a combination of program point and variable class.
 */
@Data
public class HeapObject implements Cloneable {
    private final ProgramPoint programPoint;
    private final Class<?> clazz;

    @Override
    public HeapObject clone() {
        ProgramPoint programPointClone = this.programPoint.clone();
        return new HeapObject(programPointClone, clazz);
    }
}
