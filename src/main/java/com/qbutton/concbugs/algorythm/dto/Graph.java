package com.qbutton.concbugs.algorythm.dto;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
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

        neighbors.forEach((node, edges) -> clonedMap.put(node.clone(), ImmutableSet.copyOf(edges)));

        return new Graph(clonedMap);
    }


    public Graph withEdge(HeapObject from, HeapObject to) {
        Map<HeapObject, Set<HeapObject>> newNeighbors = new HashMap<>(neighbors);

        ImmutableSet<HeapObject> newTo = ImmutableSet.of(to);
        newNeighbors.merge(from, newTo, Sets::union);

        newNeighbors.putIfAbsent(to, Collections.emptySet());

        return new Graph(newNeighbors);
    }
}
