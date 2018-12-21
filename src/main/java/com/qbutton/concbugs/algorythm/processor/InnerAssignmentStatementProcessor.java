package com.qbutton.concbugs.algorythm.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;

import java.util.ArrayList;
import java.util.List;

public final class InnerAssignmentStatementProcessor extends AbstractStatementProcessor<InnerAssignmentStatement> {

    @Override
    State process(InnerAssignmentStatement statement, State originalState) {

        EnvEntry newEnvEntry = new EnvEntry(
                statement.getVarName(),
                new HeapObject(
                        new ProgramPoint(statement.getVarName(), statement.getLineNumber()),
                        statement.getClazz()
                ));

        List<EnvEntry> newEnv = new ArrayList<>(originalState.getEnvironment());
        newEnv.add(newEnvEntry);

        return new State(
                originalState.getGraph().clone(),
                ImmutableSet.copyOf(originalState.getRoots()),
                ImmutableList.copyOf(originalState.getLocks()),
                newEnv,
                ImmutableSet.copyOf(originalState.getWaits())
        );
    }
}
