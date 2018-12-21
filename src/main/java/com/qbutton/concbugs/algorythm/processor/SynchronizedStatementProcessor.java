package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class SynchronizedStatementProcessor extends AbstractStatementProcessor<SynchronizedStatement> {

    private static final Logger LOGGER = Logger.getLogger(SynchronizedStatementProcessor.class.getName());

    private final VisitorService visitorService;

    @Override
    State process(SynchronizedStatement statement, State originalState) {

        Optional<HeapObject> hoOptional = originalState.getEnvironment().stream()
                .filter(envEntry -> statement.getVarName().equals(envEntry.getVarName()))
                .map(EnvEntry::getHeapObject)
                .findAny();

        if (!hoOptional.isPresent()) {
            //it might be a field reference, add it to envs
            LOGGER.warning("no envEntry found for varName " + statement.getVarName() + ", may be a field reference");
            HeapObject newHeapObject = new HeapObject(ProgramPoint.UNKNOWN, statement.getClassName());
            hoOptional = Optional.of(newHeapObject);

            List<EnvEntry> newEnv = new ArrayList<>(originalState.getEnvironment());
            newEnv.add(new EnvEntry(statement.getVarName(), newHeapObject));

            originalState = new State(
                    originalState.getGraph(), originalState.getRoots(), originalState.getLocks(), newEnv, originalState.getWaits()
            );
        }

        HeapObject heapObject = hoOptional.get();

        List<HeapObject> originalLocks = originalState.getLocks();
        State mergedState;
        if (originalLocks.contains(heapObject)) {
            mergedState = originalState.clone();
        } else {
            mergedState = patchState(originalState, heapObject, originalLocks);
        }

        State visitResultState = visitorService.visitStatement(statement.getInnerStatement(), mergedState);

        return new State(
                visitResultState.getGraph(),
                visitResultState.getRoots(),
                originalLocks,
                visitResultState.getEnvironment(),
                visitResultState.getWaits());
    }

    /**
     * Add object to graph under current lock, or as root if no locks held
     * @param originalState originalState
     * @param heapObject object
     * @param originalLocks originalLocks
     * @return new state
     */
    private State patchState(State originalState, HeapObject heapObject, List<HeapObject> originalLocks) {
        State mergedState;
        Graph newGraph;
        Set<HeapObject> newRoots;
        List<HeapObject> newLocks = new ArrayList<>(originalLocks);
        newLocks.add(heapObject);
        if (originalLocks.isEmpty()) {
            Map<HeapObject, Set<HeapObject>> newGraphMap = new HashMap<>(originalState.getGraph().getNeighbors());
            newGraphMap.putIfAbsent(heapObject, Collections.emptySet());
            newGraph = new Graph(newGraphMap);
            newRoots = Sets.union(ImmutableSet.of(heapObject), originalState.getRoots());

        } else {
            newGraph = originalState.getGraph().withEdge(originalLocks.get(originalLocks.size() - 1), heapObject);
            newRoots = ImmutableSet.copyOf(originalState.getRoots());
        }
        mergedState = new State(
                newGraph,
                newRoots,
                newLocks,
                ImmutableList.copyOf(originalState.getEnvironment()),
                ImmutableSet.copyOf(originalState.getWaits())
        );
        return mergedState;
    }
}
