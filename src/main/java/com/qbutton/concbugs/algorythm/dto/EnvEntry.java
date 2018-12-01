package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

@Data
public final class EnvEntry {
    private final String varName;
    private final HeapObject heapObject;
}