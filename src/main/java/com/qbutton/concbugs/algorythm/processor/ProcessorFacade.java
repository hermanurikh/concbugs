package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import lombok.RequiredArgsConstructor;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class ProcessorFacade {

    private final ProcessorProvider processorProvider;

    private static final Logger LOGGER = Logger.getLogger(ProcessorFacade.class.getName());

    public <T extends Statement> State process(T statement, State originalState) {
        AbstractStatementProcessor<T> processor = processorProvider.get(statement);
        LOGGER.info("processing statement " + statement);
        LOGGER.info("current state is " + originalState);
        return processor.process(statement, originalState);
    }
}
