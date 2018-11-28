package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Graph - directed graph of heap objects.
 */
@Data
public class Graph implements Cloneable {
    private final Map<HeapObject, Set<HeapObject>> neighbors;

    @Override
    public Graph clone() {
        Map<HeapObject, Set<HeapObject>> clonedMap = new HashMap<>();

        neighbors.forEach((node, edges) -> clonedMap.put(node.clone(),  new HashSet<>(edges)));

        return new Graph(clonedMap);
    }
}
