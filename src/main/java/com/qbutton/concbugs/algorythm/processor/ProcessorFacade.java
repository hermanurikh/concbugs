package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.statement.Statement;

public final class ProcessorFacade {
    private ProcessorFacade() {

    }

    public static <T extends Statement> void process(T statement) {
        AbstractStatementProcessor<T> processor = ProcessorProvider.get(statement);
        processor.process(statement);
    }
}
