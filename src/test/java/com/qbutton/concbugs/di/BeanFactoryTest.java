package com.qbutton.concbugs.di;

import com.qbutton.concbugs.algorythm.AlgorythmFacade;
import com.qbutton.concbugs.algorythm.processor.BranchStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.CrossAssignmentStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.DeclarationStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.InnerAssignmentStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.MethodStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.ProcessorFacade;
import com.qbutton.concbugs.algorythm.processor.ProcessorProvider;
import com.qbutton.concbugs.algorythm.processor.SequentialStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.SynchronizedStatementProcessor;
import com.qbutton.concbugs.algorythm.processor.WaitStatementProcessor;
import com.qbutton.concbugs.algorythm.service.ClassFinderService;
import com.qbutton.concbugs.algorythm.service.GraphService;
import com.qbutton.concbugs.algorythm.service.MergeService;
import com.qbutton.concbugs.algorythm.service.StateService;
import com.qbutton.concbugs.algorythm.service.VisitorService;
import com.qbutton.concbugs.inspection.deadlock.mapping.PsiToAlgorythmFacade;
import com.qbutton.concbugs.inspection.deadlock.mapping.StatementMapper;
import com.qbutton.concbugs.inspection.deadlock.mapping.StatementParser;
import com.qbutton.concbugs.inspection.deadlock.mapping.StatementShrinker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Bean factory")
class BeanFactoryTest {

    @DisplayName("correctly resolves")
    @ParameterizedTest(name = "class {arguments}")
    @ValueSource(classes = {
            PsiToAlgorythmFacade.class,
            StatementParser.class,
            StatementMapper.class,
            StatementShrinker.class,

            AlgorythmFacade.class,
            ProcessorFacade.class,
            ProcessorProvider.class,
            BranchStatementProcessor.class,
            CrossAssignmentStatementProcessor.class,
            DeclarationStatementProcessor.class,
            InnerAssignmentStatementProcessor.class,
            MethodStatementProcessor.class,
            SequentialStatementProcessor.class,
            SynchronizedStatementProcessor.class,
            WaitStatementProcessor.class,

            GraphService.class,
            VisitorService.class,
            StateService.class,
            MergeService.class,
            ClassFinderService.class
    })
    <T> void getPsiToAlgorythmFacadeBean(Class<T> candidate) {
        T bean = BeanFactory.getBean(candidate);

        assertNotNull(bean);
    }

    @DisplayName("has correctly injected statementParser to statementMapper via setter")
    @Test
    void statementParserInjected() {
        StatementMapper bean = BeanFactory.getBean(StatementMapper.class);

        assertNotNull(bean);
        assertNotNull(bean.getStatementParser());
    }

    @DisplayName("has correctly injected processorFacade to visitorService via setter")
    @Test
    void processorFacadeInjected() {
        VisitorService bean = BeanFactory.getBean(VisitorService.class);

        assertNotNull(bean);
        assertNotNull(bean.getProcessorFacade());
    }
}