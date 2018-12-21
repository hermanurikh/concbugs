package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WaitStatementProcessor extends AbstractStatementProcessor<WaitStatement> {

    @Override
    State process(WaitStatement statement, State originalState) {

        HeapObject heapObject = originalState.getEnvironment().stream()
                .filter(envEntry -> statement.getVarName().equals(envEntry.getVarName()))
                .map(EnvEntry::getHeapObject)
                .findAny()
                .orElseThrow(() -> new AlgorithmValidationException("no envEntry found for varName " + statement.getVarName()));

        Set<HeapObject> newWaits = new HashSet<>(originalState.getWaits());

        Graph originalGraph = originalState.getGraph();
        Graph newGraph = originalGraph.clone();

        List<HeapObject> currentLocks = originalState.getLocks();
        if (currentLocks.isEmpty()) {
            newWaits.add(heapObject);
        } else {
            HeapObject lastLock = currentLocks.get(currentLocks.size() - 1);
            if (!lastLock.equals(heapObject)) {

                if (!originalGraph.getNeighbors().containsKey(lastLock)) {
                    throw new AlgorithmValidationException("GraphMap is expected to contain lock object, but it does not: " + originalGraph.getNeighbors());
                }
                newGraph = originalGraph.withEdge(lastLock, heapObject);
            }
        }

        return new State(
                newGraph,
                ImmutableSet.copyOf(originalState.getRoots()),
                ImmutableList.copyOf(originalState.getLocks()),
                ImmutableList.copyOf(originalState.getEnvironment()),
                newWaits
        );
    }
}
