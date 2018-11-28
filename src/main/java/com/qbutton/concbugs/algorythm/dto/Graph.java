package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Graph - directed graph of heap objects.
 */
@Data
public class Graph {
    private final Map<HeapObject, List<HeapObject>> neighbors;
}
