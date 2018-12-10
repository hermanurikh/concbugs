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

    public ProcessorProvider(BranchStatementProcessor branchStatementProcessor,
                             CrossAssignmentStatementProcessor crossAssignmentStatementProcessor,
                             DeclarationStatementProcessor declarationStatementProcessor,
                             InnerAssignmentStatementProcessor innerAssignmentStatementProcessor,
                             MethodStatementProcessor methodStatementProcessor,
                             SequentialStatementProcessor sequentialStatementProcessor,
                             SynchronizedStatementProcessor synchronizedStatementProcessor,
                             WaitStatementProcessor waitStatementProcessor) {

        registerStatement(BranchStatement.class).withProcessor(branchStatementProcessor);
        registerStatement(CrossAssignmentStatement.class).withProcessor(crossAssignmentStatementProcessor);
        registerStatement(DeclarationStatement.class).withProcessor(declarationStatementProcessor);
        registerStatement(InnerAssignmentStatement.class).withProcessor(innerAssignmentStatementProcessor);
        registerStatement(MethodStatement.class).withProcessor(methodStatementProcessor);
        registerStatement(SequentialStatement.class).withProcessor(sequentialStatementProcessor);
        registerStatement(SynchronizedStatement.class).withProcessor(synchronizedStatementProcessor);
        registerStatement(WaitStatement.class).withProcessor(waitStatementProcessor);
    }

    @SuppressWarnings("unchecked")
    <T extends Statement> AbstractStatementProcessor<T> get(T statement) {
        return (AbstractStatementProcessor<T>) processors.get(statement.getClass());
    }

    @Data
    private class StatementProcessorMapBuilder<T extends Statement> {

        private final Class<T> clazz;
        private void withProcessor(AbstractStatementProcessor<T> processor) {
            processors.put(clazz, processor);
        }
    }

    private <T extends Statement> StatementProcessorMapBuilder<T> registerStatement(Class<T> clazz) {
        return new StatementProcessorMapBuilder<>(clazz);
    }

    private final Map<Class<? extends Statement>,
            AbstractStatementProcessor<? extends Statement>> processors = new HashMap<>();
}
