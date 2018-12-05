package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;

public final class ProcessorFacade {
    private ProcessorFacade() {

    }

    public static <T extends Statement> State process(T statement, State originalState) {
        AbstractStatementProcessor<T> processor = ProcessorProvider.get(statement);
        return processor.process(statement, originalState);
    }
}
