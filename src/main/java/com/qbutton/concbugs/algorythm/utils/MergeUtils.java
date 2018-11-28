package com.qbutton.concbugs.algorythm.utils;

import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;

import java.util.Map;
import java.util.Set;

public final class MergeUtils {

    public static Graph removeObject(Graph graph, HeapObject heapObject) {
        Graph updatedGraph = graph.clone();
        Map<HeapObject, Set<HeapObject>> neighbors = updatedGraph.getNeighbors();

        //remove node itself
        neighbors.remove(heapObject);

        //remove connections to this node
        neighbors.values().forEach(edges -> edges.remove(heapObject));

        return updatedGraph;
    }

    public static Graph mergeGraphs(Graph g1, Graph g2) {
        Graph updatedGraph = g1.clone();

        g2.getNeighbors().forEach((node, edges) ->
                updatedGraph.getNeighbors().merge(node, edges, Sets::union));

        return updatedGraph;
    }

    private MergeUtils() {}
}
