package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SequentialStatementProcessor extends AbstractStatementProcessor<SequentialStatement> {

    private final VisitorService visitorService;

    @Override
    State process(SequentialStatement statement, State originalState) {
        State firstState = visitorService.visitStatement(statement.getStmt1(), originalState);
        State secondState = visitorService.visitStatement(statement.getStmt2(), firstState);

        return secondState;
    }
}
