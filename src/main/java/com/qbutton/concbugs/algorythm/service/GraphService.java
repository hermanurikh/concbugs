package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphService {

    public Graph removeObject(Graph graph, HeapObject heapObject) {

        ReplaceNodeResult replaceNodeResult = replaceNode(graph, Collections.emptySet(), heapObject, null);

        return replaceNodeResult.graph;
    }

    public ReplaceNodeResult replaceNode(Graph graph,
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
            //a merge is needed because this node might already be in the set
            neighbors.merge(newHo, oldEdges, (oldObjects, newObjects) -> new HashSet<>(Sets.union(oldEdges, newObjects)));
            //ensure there is no self-link
            neighbors.get(newHo).remove(newHo);
        }

        //remove connections to this node and add new, if it is a replace operation
        neighbors.forEach((node, edges) -> {
            boolean wasPresent = edges.remove(oldHO);
            //do not allow self-link - we cannot obtain the lock twice
            if (wasPresent && needToReplace && newHo != node) {
                edges.add(newHo);
            }
        });

        Set<HeapObject> updatedRoots = new HashSet<>(roots);
        if (updatedRoots.remove(oldHO) && needToReplace) {
            updatedRoots.add(newHo);
        }

        return new ReplaceNodeResult(updatedGraph, updatedRoots);
    }

    /**
     * Splice out the node.
     * For each incoming edge, draw a new edge to each of children (outcoming edges).
     * If roots contained this object, replace it with all reachable (outgoing) objects.
     * @param graph graph
     * @param roots roots
     * @param oldHO node to splice out
     * @return result of splicing out
     */
    public ReplaceNodeResult spliceOutNode(Graph graph,
                                                  Set<HeapObject> roots,
                                                  HeapObject oldHO) {
        Graph updatedGraph = graph.clone();
        Map<HeapObject, Set<HeapObject>> neighbors = updatedGraph.getNeighbors();

        //remove node itself
        Set<HeapObject> oldEdges = neighbors.remove(oldHO);

        if (oldEdges == null) {
            throw new AlgorithmValidationException(
                    String.format("graph %s does not contain heapObject %s, it cannot be spliced out", graph, oldHO));
        }

        //remove edges to this node. If there was one, replace node with all outgoing edges
        neighbors.forEach((node, edges) -> {
            boolean wasPresent = edges.remove(oldHO);
            if (wasPresent) {
                edges.addAll(oldEdges);
            }

            //ensure there is no self-link
            edges.remove(node);
        });

        Set<HeapObject> updatedRoots = new HashSet<>(roots);
        if (updatedRoots.remove(oldHO)) {
            updatedRoots.addAll(oldEdges);
        }

        return new ReplaceNodeResult(updatedGraph, updatedRoots);
    }

    @Data
    public static class ReplaceNodeResult {
        private final Graph graph;
        private final Set<HeapObject> roots;
    }
}
