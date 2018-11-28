package com.qbutton.concbugs.algorythm.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Graph - directed graph of heap objects.
 */
@Data
public class Graph implements Cloneable {
    private final Map<HeapObject, List<HeapObject>> neighbors;

    @Override
    public Graph clone() {
        Map<HeapObject, List<HeapObject>> clonedMap = new HashMap<>();

        //todo check if LinkedList is appropriate here
        neighbors.forEach((heapObj, list) -> clonedMap.put(heapObj.clone(),  new LinkedList<>(list)));

        return new Graph(clonedMap);
    }
}
