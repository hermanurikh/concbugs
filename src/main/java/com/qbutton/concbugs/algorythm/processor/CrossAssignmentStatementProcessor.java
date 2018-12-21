package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import com.qbutton.concbugs.algorythm.service.GraphService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class CrossAssignmentStatementProcessor extends AbstractStatementProcessor<CrossAssignmentStatement> {

    private final GraphService graphService;

    @Override
    State process(CrossAssignmentStatement statement, State originalState) {

        String originalVar = statement.getRightValueName();

        HeapObject existingHeapObject = originalState.getEnvironment().stream()
                .filter(envEntry -> originalVar.equals(envEntry.getVarName()))
                .findAny()
                .map(EnvEntry::getHeapObject)
                .orElseThrow(() -> new AlgorithmValidationException("no envEntry found for varName " + originalVar));

        List<EnvEntry> newEnv = graphService.addOrReplaceEnv(
                new EnvEntry(statement.getVarName(), existingHeapObject), originalState.getEnvironment()
        );

        return new State(
                originalState.getGraph().clone(),
                ImmutableSet.copyOf(originalState.getRoots()),
                ImmutableList.copyOf(originalState.getLocks()),
                newEnv,
                ImmutableSet.copyOf(originalState.getWaits())
        );
    }
}
