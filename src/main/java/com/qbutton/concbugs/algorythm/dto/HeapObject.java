package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

/**
 * Heap object - a combination of program point and variable class.
 */
@Data
public class HeapObject {
    private final ProgramPoint programPoint;
    private final Class<?> clazz;
}
