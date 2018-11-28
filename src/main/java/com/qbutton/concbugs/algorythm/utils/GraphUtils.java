package com.qbutton.concbugs.algorythm.utils;

import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GraphUtils {

    public static Graph removeObject(Graph graph, HeapObject heapObject) {

        ReplaceNodeResult replaceNodeResult = replaceNode(graph, Collections.emptySet(), heapObject, null);

        return replaceNodeResult.graph;
    }

    public static ReplaceNodeResult replaceNode(Graph graph,
                                                Set<HeapObject> roots,
                                                HeapObject oldHO,
                                                HeapObject newHo) {
        Graph updatedGraph = graph.clone();
        Map<HeapObject, Set<HeapObject>> neighbors = updatedGraph.getNeighbors();
        boolean needToReplace = newHo != null;

        //remove node itself
        Set<HeapObject> oldEdges = neighbors.remove(oldHO);

        if (oldEdges == null) {
            throw new AlgorithmValidationException(
                    String.format("graph %s does not contain heapObject %s, it cannot be removed", graph, oldHO));
        }

        //if this is a replace, not a remove operation, put a new node
        if (needToReplace) {
            neighbors.put(newHo, oldEdges);
        }

        //remove connections to this node and add new, if it is a replace operation
        neighbors.values().forEach(edges -> {
            boolean wasPresent = edges.remove(oldHO);
            if (wasPresent && needToReplace) {
                edges.add(newHo);
            }
        });

        Set<HeapObject> updatedRoots = new HashSet<>(roots);
        if (updatedRoots.remove(oldHO) && needToReplace) {
            updatedRoots.add(newHo);
        }

        return new ReplaceNodeResult(updatedGraph, updatedRoots);
    }

    @Data
    public static class ReplaceNodeResult {
        private final Graph graph;
        private final Set<HeapObject> roots;
    }

    private GraphUtils() {
    }
}
