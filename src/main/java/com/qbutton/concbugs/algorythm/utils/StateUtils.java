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

        List<HeapObject> formalParameters = getMethodParameters(returnedMethodState);
        List<HeapObject> actualParameters = getMethodParameters(currentState);

        validateMethodParams(formalParameters, actualParameters);

        ReplaceNodeResult replaceNodeResult =
                mergeGraphsAndRoots(returnedMethodState, currentState, formalParameters, actualParameters);

        Set<HeapObject> newWaitSet = mergeWaits(returnedMethodState, formalParameters, actualParameters);

        return new State(
                replaceNodeResult.getGraph(),
                replaceNodeResult.getRoots(),
                returnedMethodState.getLocks(),
                returnedMethodState.getEnvironment(),
                newWaitSet
        );
    }

    private static ReplaceNodeResult mergeGraphsAndRoots(State returnedMethodState,
                                                         State currentState,
                                                         List<HeapObject> formalParameters,
                                                         List<HeapObject> actualParameters) {
        ReplaceNodeResult replaceNodeResult =
                new ReplaceNodeResult(returnedMethodState.getGraph(), returnedMethodState.getRoots());

        //for all objects locked by the callee
        for (HeapObject lockedHeapObject : replaceNodeResult.getGraph().getNeighbors().keySet()) {
            Graph currentCalleeGraph = replaceNodeResult.getGraph();
            Set<HeapObject> currentCalleeRoots = replaceNodeResult.getRoots();

            int index = formalParameters.indexOf(lockedHeapObject);
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
        }

        return replaceNodeResult;
    }

    private static Set<HeapObject> mergeWaits(State returnedMethodState,
                                              List<HeapObject> formalParameters,
                                              List<HeapObject> actualParameters) {
        Set<HeapObject> newWaitSet = new HashSet<>();
        returnedMethodState.getWaits().forEach(wait -> {
            int index = formalParameters.indexOf(wait);

            if (index >= 0) {
                newWaitSet.add(actualParameters.get(index));
            } else {
                newWaitSet.add(new HeapObject(ProgramPoint.UNKNOWN, wait.getClazz()));
            }
        });
        return newWaitSet;
    }

    private static List<HeapObject> getMethodParameters(State returnedMethodState) {
        return returnedMethodState.getEnvironment()
                .stream()
                .map(EnvEntry::getHeapObject)
                .collect(toList());
    }

    private static void validateMethodParams(List<HeapObject> formalParameters, List<HeapObject> actualParameters) {
        if (formalParameters.size() != actualParameters.size()) {
            throw new AlgorithmValidationException("formal and actual method parameters should have same size");
        }
    }

    private StateUtils() {
    }
}