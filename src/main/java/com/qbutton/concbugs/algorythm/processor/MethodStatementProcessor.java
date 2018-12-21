package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.Sets;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import com.qbutton.concbugs.algorythm.service.GraphService;
import com.qbutton.concbugs.algorythm.service.MergeService;
import com.qbutton.concbugs.algorythm.service.StateService;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class MethodStatementProcessor extends AbstractStatementProcessor<MethodStatement> {

    private final VisitorService visitorService;
    private final StateService stateService;
    private final MergeService mergeService;
    private final GraphService graphService;

    @Override
    State process(MethodStatement statement, State originalState) {
        List<EnvEntry> newEnv = registerMethodResultInEnv(statement, originalState);

        State currentState = new State(
                originalState.getGraph(), originalState.getRoots(), originalState.getLocks(), newEnv, originalState.getWaits()
        );

        for (MethodDeclaration method : statement.getMethodDeclarations()) {
            currentState = mergeMethod(originalState, newEnv, currentState, method, statement.getActualParameters());
        }

        return currentState;
    }

    private List<EnvEntry> registerMethodResultInEnv(MethodStatement statement, State originalState) {
        if (statement.getVarName() != null) {
            ProgramPoint newProgramPoint = new ProgramPoint(statement.getVarName(), statement.getLineNumber());
            HeapObject returnVarHeapObject = new HeapObject(newProgramPoint, statement.getReturnType());
            EnvEntry newEnvEntry = new EnvEntry(statement.getVarName(), returnVarHeapObject);

            return graphService.addOrReplaceEnv(newEnvEntry, originalState.getEnvironment());
        }

        return originalState.getEnvironment();
    }

    private State mergeMethod(State originalState,
                              List<EnvEntry> newEnv,
                              State currentState,
                              MethodDeclaration method,
                              List<String> actualStringParameters) {
        State returnedMethodState = visitorService.visitMethod(method);
        //the fist envs in env will be formal parameters, including this
        List<HeapObject> formalParameters = returnedMethodState.getEnvironment().stream()
                .map(EnvEntry::getHeapObject)
                .limit(method.getVariables().size())
                .collect(Collectors.toList());

        List<HeapObject> actualParameters = getActualParameters(originalState, actualStringParameters, formalParameters);

        State renamedState = stateService.renameFromCalleeToCallerContext(
                returnedMethodState, currentState, formalParameters, actualParameters
        );
        Graph newGraph = mergeService.mergeGraphs(currentState.getGraph(), renamedState.getGraph());

        Set<HeapObject> newRoots = currentState.getRoots();
        Set<HeapObject> newWaits = currentState.getWaits();

        if (originalState.getLocks().isEmpty()) {
            //connect current lock to roots of returnedMethodState
            newRoots = Sets.union(newRoots, renamedState.getRoots());
            newWaits = Sets.union(newWaits, renamedState.getWaits());
        } else {
            HeapObject lastLock = originalState.getLocks().get(originalState.getLocks().size() - 1);

            checkGraphContainsLock(newGraph, lastLock);

            for (HeapObject root : renamedState.getRoots()) {
                newGraph = newGraph.withEdge(lastLock, root);
            }
            for (HeapObject wait : renamedState.getWaits()) {
                if (lastLock != wait) {
                    newGraph = newGraph.withEdge(lastLock, wait);
                }
            }
        }

        return new State(newGraph, newRoots, originalState.getLocks(), newEnv, newWaits);
    }

    private List<HeapObject> getActualParameters(State originalState,
                                                 List<String> actualStringParameters,
                                                 List<HeapObject> formalParameters) {
        return actualStringParameters.isEmpty()
                ?
                //top level method, replace with unknown heap objects
                formalParameters.stream()
                        .map(ho -> new HeapObject(ProgramPoint.UNKNOWN, ho.getClazz()))
                        .collect(Collectors.toList())
                :
                actualStringParameters.stream()
                        .map(varName -> findEntryInOriginalEnv(originalState, varName))
                        .map(EnvEntry::getHeapObject)
                        .collect(Collectors.toList());
    }

    private EnvEntry findEntryInOriginalEnv(State originalState, String varName) {
        return originalState.getEnvironment()
                .stream()
                .filter(envEntry -> envEntry.getVarName().equals(varName))
                .findAny()
                .orElseThrow(() -> new AlgorithmValidationException("Original env does not contain variable with name " + varName));
    }

    private void checkGraphContainsLock(Graph newGraph, HeapObject lastLock) {
        if (!newGraph.getNeighbors().containsKey(lastLock)) {
            throw new AlgorithmValidationException("Graph does not contain lock " + lastLock);
        }
    }
}
