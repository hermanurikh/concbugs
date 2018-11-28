package com.qbutton.concbugs.algorythm.utils;

import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MergeUtils {

    public static Graph mergeGraphs(Graph g1, Graph g2) {
        Graph updatedGraph = g1.clone();

        g2.getNeighbors().forEach((node, edges) ->
                updatedGraph.getNeighbors().merge(node, edges, Sets::union));

        return updatedGraph;
    }

    public static State mergeStates(State s1, State s2, int lineNumber) {
        Graph mergedGraph = mergeGraphs(s1.getGraph(), s2.getGraph());
        Set<HeapObject> mergedRoots = Sets.union(s1.getRoots(), s2.getRoots());

        //because hierarchy of synchronized guarantees locks to be the same
        List<HeapObject> mergedLocks = new ArrayList<>(s1.getLocks());
        Set<HeapObject> mergedWaits = Sets.union(s1.getWaits(), s2.getWaits());

        Map<String, HeapObject> mergedEnvs
                = mergeEnvs(s1.getEnvironment(), s2.getEnvironment(), lineNumber);

        return new State(mergedGraph, mergedRoots, mergedLocks, mergedEnvs, mergedWaits);
    }

    /**
     * Merges 2 environments.
     *
     * The new environment remains the same for mappings common to both paths.
     * If the mappings differ for a given variable then a fresh heap object must be
     * introduced for that variable.
     * The fresh object is assigned a program point corresponding to the join point
     * for the variable (each variable is considered to join at a separate location). The
     * strongest type constraint for the fresh object is the join of the variables' types
     * along each path - their lowest common superclass.
     *
     * @param env1 environment1
     * @param env2 environment2
     * @param lineNumber line number where merge happens
     * @return merged environment
     */
    public static Map<String, HeapObject> mergeEnvs(Map<String, HeapObject> env1,
                                                    Map<String, HeapObject> env2,
                                                    int lineNumber) {
        if (!env1.keySet().equals(env2.keySet())) {
            String message = String.format(
                    "The keysets of two envs [%s, %s] are different, merging seems to be incorrect.",
                    env1.keySet(), env2.keySet());
            throw new AlgorithmValidationException(message);
        }

        Map<String, HeapObject> mergedEnv = new HashMap<>(env1.size());

        env1.keySet().forEach(varName -> {
            HeapObject ho1 = env1.get(varName);
            HeapObject ho2 = env2.get(varName);
            if (ho1.equals(ho2)) {
                mergedEnv.put(varName, ho1);
            } else {
                Class<?> lowestSuperClass = findLowestSuperClass(ho1.getClazz(), ho2.getClazz());
                ProgramPoint freshProgramPoint = new ProgramPoint(varName, lineNumber);
                mergedEnv.put(varName, new HeapObject(freshProgramPoint, lowestSuperClass));
            }
        });

        return mergedEnv;
    }

    public static Class<?> findLowestSuperClass(Class<?> class1, Class<?> class2) {
        while (!class1.isAssignableFrom(class2))
            class1 = class1.getSuperclass();
        return class1;
    }


    private MergeUtils() {
    }
}
