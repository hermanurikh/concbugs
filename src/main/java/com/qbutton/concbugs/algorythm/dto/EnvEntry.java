package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

@Data
public class EnvEntry {
    private final String varName;
    private final HeapObject heapObject;
}