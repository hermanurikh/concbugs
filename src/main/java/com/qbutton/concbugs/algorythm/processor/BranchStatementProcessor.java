package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.service.MergeService;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class BranchStatementProcessor extends AbstractStatementProcessor<BranchStatement> {

    private final VisitorService visitorService;
    private final MergeService mergeService;

    @Override
    State process(BranchStatement statement, State originalState) {
        //todo use fork-join?
        State firstBranchState =
                statement.getStmt1() != null
                        ? visitorService.visitStatement(statement.getStmt1(), originalState)
                        : State.EMPTY_STATE;
        State secondBranchState =
                statement.getStmt2() != null
                        ? visitorService.visitStatement(statement.getStmt2(), originalState)
                        : State.EMPTY_STATE;

        return mergeService.mergeStates(firstBranchState, secondBranchState, statement.getLineNumber());
    }
}
