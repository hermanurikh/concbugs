package com.qbutton.concbugs.algorythm.processor;

import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

final class ProcessorProvider {

    static {
        registerStatement(BranchStatement.class).withProcessor(new BranchStatementProcessor());
        registerStatement(CrossAssignmentStatement.class).withProcessor(new CrossAssignmentStatementProcessor());
        registerStatement(DeclarationStatement.class).withProcessor(new DeclarationStatementProcessor());
        registerStatement(InnerAssignmentStatement.class).withProcessor(new InnerAssignmentStatementProcessor());
        registerStatement(MethodStatement.class).withProcessor(new MethodStatementProcessor());
        registerStatement(SequentialStatement.class).withProcessor(new SequentialStatementProcessor());
        registerStatement(SynchronizedStatement.class).withProcessor(new SynchronizedStatementProcessor());
        registerStatement(WaitStatement.class).withProcessor(new WaitStatementProcessor());
    }

    @SuppressWarnings("unchecked")
    static <T extends Statement> AbstractStatementProcessor<T> get(T statement) {
        return (AbstractStatementProcessor<T>) PROCESSORS.get(statement.getClass());
    }

    private ProcessorProvider() { }

    @Data
    private static class StatementProcessorMapBuilder<T extends Statement> {

        private final Class<T> clazz;
        private void withProcessor(AbstractStatementProcessor<T> processor) {
            PROCESSORS.put(clazz, processor);
        }
    }

    private static <T extends Statement> StatementProcessorMapBuilder<T> registerStatement(Class<T> clazz) {
        return new StatementProcessorMapBuilder<>(clazz);
    }

    private static final Map<Class<? extends Statement>,
            AbstractStatementProcessor<? extends Statement>> PROCESSORS = new HashMap<>();
}
