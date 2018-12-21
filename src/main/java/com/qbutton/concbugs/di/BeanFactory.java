package com.qbutton.concbugs.di;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
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
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.util.logging.Logger;

public final class BeanFactory {
    private BeanFactory() {
    }

    private static final Logger LOGGER = Logger.getLogger(BeanFactory.class.getName());


    private static MutablePicoContainer PICO = new DefaultPicoContainer();

    static {
        PICO.registerComponentImplementation(PsiToAlgorythmFacade.class);
        PICO.registerComponentImplementation(StatementParser.class);
        PICO.registerComponentImplementation(StatementMapper.class);
        PICO.registerComponentImplementation(StatementShrinker.class);

        setStatementParserToStatementMapper();

        PICO.registerComponentImplementation(AlgorythmFacade.class);
        PICO.registerComponentImplementation(ProcessorFacade.class);
        PICO.registerComponentImplementation(ProcessorProvider.class);
        PICO.registerComponentImplementation(BranchStatementProcessor.class);
        PICO.registerComponentImplementation(CrossAssignmentStatementProcessor.class);
        PICO.registerComponentImplementation(DeclarationStatementProcessor.class);
        PICO.registerComponentImplementation(InnerAssignmentStatementProcessor.class);
        PICO.registerComponentImplementation(MethodStatementProcessor.class);
        PICO.registerComponentImplementation(SequentialStatementProcessor.class);
        PICO.registerComponentImplementation(SynchronizedStatementProcessor.class);
        PICO.registerComponentImplementation(WaitStatementProcessor.class);

        PICO.registerComponentImplementation(GraphService.class);
        PICO.registerComponentImplementation(VisitorService.class);
        PICO.registerComponentImplementation(StateService.class);
        PICO.registerComponentImplementation(MergeService.class);
        PICO.registerComponentImplementation(ClassFinderService.class);

        setProjectToClassFinderService();
        setProcessorFacadeToVisitorService();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> tClass) {
        return (T) PICO.getComponentInstance(tClass);
    }

    private static void setStatementParserToStatementMapper() {
        //avoiding cyclic dependency here and whereafter
        getBean(StatementMapper.class).setStatementParser(getBean(StatementParser.class));
    }

    private static void setProcessorFacadeToVisitorService() {
        getBean(VisitorService.class).setProcessorFacade(getBean(ProcessorFacade.class));
    }

    private static void setProjectToClassFinderService() {
        //not sure how to lookup project as there is no such bean
        try {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            if (openProjects.length > 0) {
                getBean(ClassFinderService.class).setProject(openProjects[0]);
            }
        } catch (NullPointerException ex) {
            //this is weird, but IDEA doesn't give a chance and throws directly NPE inside ProjectManager.getInstance()
            LOGGER.warning("no project manager found, no project will be set to ClassFinderService");
        }
    }
}
