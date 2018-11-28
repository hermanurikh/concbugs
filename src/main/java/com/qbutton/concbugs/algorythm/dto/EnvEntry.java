package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

/**
 * Environment entry is a mapping of local variable on a concrete heap object.
 */
@Data
public class EnvEntry {
    private final String varName;
    private final HeapObject heapObject;
}
