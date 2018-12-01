package com.qbutton.concbugs.algorythm.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Graph - directed graph of heap objects.
 */
@Data
public final class Graph implements Cloneable {
    private final Map<HeapObject, Set<HeapObject>> neighbors;

    @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
    @Override
    public Graph clone() {
        Map<HeapObject, Set<HeapObject>> clonedMap = new HashMap<>();

        neighbors.forEach((node, edges) -> clonedMap.put(node.clone(),  new HashSet<>(edges)));

        return new Graph(clonedMap);
    }
}
