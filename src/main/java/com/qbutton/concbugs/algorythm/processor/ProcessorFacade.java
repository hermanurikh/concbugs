package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessorFacade {

    private final ProcessorProvider processorProvider;

    public <T extends Statement> State process(T statement, State originalState) {
        AbstractStatementProcessor<T> processor = processorProvider.get(statement);
        return processor.process(statement, originalState);
    }
}
