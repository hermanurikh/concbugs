package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class SequentialStatementProcessor extends AbstractStatementProcessor<SequentialStatement> {

    private final ProcessorFacade processorFacade;

    @Override
    State process(SequentialStatement statement, State originalState) {
        State firstState = processorFacade.process(statement.getStmt1(), originalState);
        State secondState = processorFacade.process(statement.getStmt2(), firstState);

        return secondState;
    }
}
