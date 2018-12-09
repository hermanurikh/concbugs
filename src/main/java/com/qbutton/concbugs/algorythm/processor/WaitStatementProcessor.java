package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class WaitStatementProcessor extends AbstractStatementProcessor<WaitStatement> {

    @Override
    State process(WaitStatement statement, State originalState) {

        HeapObject heapObject = originalState.getEnvironment().stream()
                .filter(envEntry -> statement.getVarName().equals(envEntry.getVarName()))
                .map(EnvEntry::getHeapObject)
                .findAny()
                .orElseThrow(() -> new AlgorithmValidationException("no envEntry found for varName " + statement.getVarName()));

        Set<HeapObject> newWaits = new HashSet<>(originalState.getWaits());
        Map<HeapObject, Set<HeapObject>> newGraphMap = new HashMap<>(originalState.getGraph().getNeighbors());

        List<HeapObject> currentLocks = originalState.getLocks();
        if (currentLocks.isEmpty()) {
            newWaits.add(heapObject);
        } else {
            HeapObject lastLock = currentLocks.get(currentLocks.size() - 1);
            if (!lastLock.equals(heapObject)) {
                addObjectToGraph(heapObject, newGraphMap, lastLock);
            }
        }

        return new State(
                new Graph(newGraphMap),
                ImmutableSet.copyOf(originalState.getRoots()),
                ImmutableList.copyOf(originalState.getLocks()),
                ImmutableList.copyOf(originalState.getEnvironment()),
                newWaits
        );
    }

    private void addObjectToGraph(HeapObject heapObject, Map<HeapObject, Set<HeapObject>> newGraphMap, HeapObject lastLock) {
        // wait releases then reacquires heapObject, new lock ordering

        //add object itself
        newGraphMap.put(heapObject, Collections.emptySet());
        //draw edge to this object from latest lock
        if (!newGraphMap.containsKey(lastLock)) {
            throw new AlgorithmValidationException("GraphMap is expected to contain lock object, but it does not: " + newGraphMap);
        }
        Set<HeapObject> heapObjects = new HashSet<>(newGraphMap.get(lastLock));
        heapObjects.add(heapObject);
        newGraphMap.put(lastLock, heapObjects);
    }
}
