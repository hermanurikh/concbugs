package com.qbutton.concbugs.algorythm.utils;

import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import com.qbutton.concbugs.algorythm.utils.GraphUtils.ReplaceNodeResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.qbutton.concbugs.algorythm.utils.GraphUtils.replaceNode;
import static com.qbutton.concbugs.algorythm.utils.GraphUtils.spliceOutNode;
import static java.util.stream.Collectors.toList;

public final class StateUtils {

    public static State renameFromCalleeToCallerContext(State returnedMethodState, State currentState) {

        List<HeapObject> formalParameters = returnedMethodState.getEnvironment()
                .stream()
                .map(EnvEntry::getHeapObject)
                .collect(toList());
        List<HeapObject> actualParameters = currentState.getEnvironment()
                .stream()
                .map(EnvEntry::getHeapObject)
                .collect(toList());

        if (formalParameters.size() != actualParameters.size()) {
            throw new AlgorithmValidationException("formal and actual method parameters should have same size");
        }

        Graph currentCalleeGraph = returnedMethodState.getGraph();
        Set<HeapObject> currentCalleeRoots = returnedMethodState.getRoots();

        //for all objects locked by the callee
        for (HeapObject lockedHeapObject : currentCalleeGraph.getNeighbors().keySet()) {
            int index = formalParameters.indexOf(lockedHeapObject);
            ReplaceNodeResult replaceNodeResult;
            if (index >= 0) {
                //object is formal parameter of callee method

                if (currentState.getLocks().contains(actualParameters.get(index))) {
                    //object was locked by caller, remove object from callee graph
                    replaceNodeResult = spliceOutNode(
                            currentCalleeGraph, currentCalleeRoots, lockedHeapObject);
                } else {
                    //caller did not lock object, rename object to actual arg
                    replaceNodeResult = replaceNode(
                            currentCalleeGraph, currentCalleeRoots, lockedHeapObject, actualParameters.get(index));
                }
            } else {
                //object is not from caller, replace object with bottom program point
                replaceNodeResult = replaceNode(
                        currentCalleeGraph, currentCalleeRoots, lockedHeapObject, new HeapObject(ProgramPoint.UNKNOWN, lockedHeapObject.getClazz()));
            }

            currentCalleeGraph = replaceNodeResult.getGraph();
            currentCalleeRoots = replaceNodeResult.getRoots();
        }

        Set<HeapObject> newWaitSet = new HashSet<>();
        returnedMethodState.getWaits().forEach(wait -> {
            int index = formalParameters.indexOf(wait);

            if (index >= 0) {
                newWaitSet.add(actualParameters.get(index));
            } else {
                newWaitSet.add(new HeapObject(ProgramPoint.UNKNOWN, wait.getClazz()));
            }
        });

        return new State(
                currentCalleeGraph,
                currentCalleeRoots,
                returnedMethodState.getLocks(),
                returnedMethodState.getEnvironment(),
                newWaitSet
        );
    }

    private StateUtils() {
    }
}