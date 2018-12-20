package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MergeService {

    private final SuperClassFinderService superClassFinderService;

    public Graph mergeGraphs(Graph g1, Graph g2) {
        Graph updatedGraph = g1.clone();

        g2.getNeighbors().forEach((node, edges) ->
                updatedGraph.getNeighbors().merge(node, edges, Sets::union));

        return updatedGraph;
    }

    public State mergeStates(State s1, State s2, int lineNumber) {
        Graph mergedGraph = mergeGraphs(s1.getGraph(), s2.getGraph());
        Set<HeapObject> mergedRoots = Sets.union(s1.getRoots(), s2.getRoots());

        //because hierarchy of synchronized guarantees locks to be the same
        List<HeapObject> mergedLocks = new ArrayList<>(s1.getLocks());
        Set<HeapObject> mergedWaits = Sets.union(s1.getWaits(), s2.getWaits());

        List<EnvEntry> mergedEnvs = mergeEnvs(s1.getEnvironment(), s2.getEnvironment(), lineNumber);

        return new State(mergedGraph, mergedRoots, mergedLocks, mergedEnvs, mergedWaits);
    }

    /**
     * Merges 2 environments.
     * <p>
     * The new environment remains the same for mappings common to both paths.
     * If the mappings differ for a given variable then a fresh heap object must be
     * introduced for that variable.
     * The fresh object is assigned a program point corresponding to the join point
     * for the variable (each variable is considered to join at a separate location). The
     * strongest type constraint for the fresh object is the join of the variables' types
     * along each path - their lowest common superclass.
     *
     * @param env1       environment1
     * @param env2       environment2
     * @param lineNumber line number where merge happens
     * @return merged environment
     */
    List<EnvEntry> mergeEnvs(List<EnvEntry> env1,
                                           List<EnvEntry> env2,
                                           int lineNumber) {
        Set<String> env1Keys = getKeys(env1);
        Set<String> env2Keys = getKeys(env2);

        if (!env1Keys.equals(env2Keys)) {
            String message = String.format(
                    "The keysets of two envs [%s, %s] are different, merging seems to be incorrect.", env1, env2);
            throw new AlgorithmValidationException(message);
        }

        List<EnvEntry> mergedEnv = new ArrayList<>(env1.size());

        IntStream
                .range(0, env1.size())
                .forEach(idx -> {
                    EnvEntry env1Entry = env1.get(idx);
                    HeapObject ho1 = env1Entry.getHeapObject();
                    EnvEntry env2Entry = env2.get(idx);
                    HeapObject ho2 = env2Entry.getHeapObject();

                    if (ho1.equals(ho2)) {
                        mergedEnv.add(env1Entry);
                    } else {
                        String lowestSuperClass = superClassFinderService.findLowestSuperClass(ho1.getClazz(), ho2.getClazz());
                        ProgramPoint freshProgramPoint = new ProgramPoint(env1Entry.getVarName(), lineNumber);
                        mergedEnv.add(new EnvEntry(env1Entry.getVarName(), new HeapObject(freshProgramPoint, lowestSuperClass)));
                    }
                });

        return mergedEnv;
    }

    @NotNull
    private Set<String> getKeys(List<EnvEntry> env) {
        return env.stream().map(EnvEntry::getVarName).collect(Collectors.toSet());
    }
}
