package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class GraphService {

    private final ClassFinderService classFinderService;

    Graph removeObject(Graph graph, HeapObject heapObject) {

        ReplaceNodeResult replaceNodeResult = replaceNode(graph, Collections.emptySet(), heapObject, null);

        return replaceNodeResult.graph;
    }

    ReplaceNodeResult replaceNode(Graph graph,
                                         Set<HeapObject> roots,
                                         HeapObject oldHO,
                                         HeapObject newHo) {
        Map<HeapObject, Set<HeapObject>> neighbors = new HashMap<>(graph.getNeighbors().size());
        graph.getNeighbors().forEach((node, edges) -> neighbors.put(node, new HashSet<>(edges)));

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
            neighbors.merge(newHo, oldEdges, (oldObjects, newObjects) -> new HashSet<>(Sets.union(oldObjects, newObjects)));
            //ensure there is no self-link
            if (!newHo.getProgramPoint().isUnknown()) {
                neighbors.get(newHo).remove(newHo);
            }
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

        Graph updatedGraph = new Graph(neighbors);

        return new ReplaceNodeResult(updatedGraph, updatedRoots);
    }

    /**
     * Splice out the node.
     * For each incoming edge, draw a new edge to each of children (outcoming edges).
     * If roots contained this object, replace it with all reachable (outgoing) objects.
     *
     * @param graph graph
     * @param roots roots
     * @param oldHO node to splice out
     * @return result of splicing out
     */
    ReplaceNodeResult spliceOutNode(Graph graph,
                                           Set<HeapObject> roots,
                                           HeapObject oldHO) {
        Map<HeapObject, Set<HeapObject>> neighbors = new HashMap<>(graph.getNeighbors().size());
        graph.getNeighbors().forEach((node, edges) -> neighbors.put(node, new HashSet<>(edges)));

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

        Graph updatedGraph = new Graph(neighbors);

        return new ReplaceNodeResult(updatedGraph, updatedRoots);
    }

    public List<EnvEntry> addOrReplaceEnv(EnvEntry newEnvEntry, List<EnvEntry> oldEnv) {
        List<EnvEntry> newEnv = new ArrayList<>(oldEnv);

        OptionalInt varExists = IntStream.range(0, newEnv.size())
                .filter(i -> newEnv.get(i).getVarName().equals(newEnvEntry.getVarName()))
                .findAny();

        if (varExists.isPresent()) {
            newEnv.set(varExists.getAsInt(), newEnvEntry);
        } else {
            newEnv.add(newEnvEntry);
        }

        return newEnv;
    }

    @SuppressWarnings("unchecked")
    public Graph postProcess(List<State> fixedMethodStates) {
        Graph result = new Graph(Collections.emptyMap());

        //for each of public methods results
        for (State fixedMethodState : fixedMethodStates) {
            //for each graph edge
            for (Map.Entry<HeapObject, Set<HeapObject>> entry : fixedMethodState.getGraph().getNeighbors().entrySet()) {
                HeapObject from = entry.getKey();
                Set<HeapObject> edges = entry.getValue();
                Set<String> subclassesOfFrom = classFinderService.getSubclassesOf(from.getClazz());
                Set<String> subclassesOfTo = new HashSet<>();
                for (HeapObject to : edges) {
                    subclassesOfTo.addAll(classFinderService.getSubclassesOf(to.getClazz()));
                }

                for (String subclassOfFrom : subclassesOfFrom) {
                    for (String subclassOfTo : subclassesOfTo) {
                        HeapObject unknownHoFrom = new HeapObject(ProgramPoint.UNKNOWN, subclassOfFrom);
                        HeapObject unknownHoTo = new HeapObject(ProgramPoint.UNKNOWN, subclassOfTo);

                        result = result.withEdge(unknownHoFrom, unknownHoTo);
                    }
                }
            }
        }

        return result;
    }

    @Data
    static class ReplaceNodeResult {
        private final Graph graph;
        private final Set<HeapObject> roots;
    }
}
