package com.qbutton.concbugs.algorythm;

import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.State;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.processor.ProcessorFacade;
import com.qbutton.concbugs.algorythm.service.GraphService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AlgorythmFacade {

    private final ProcessorFacade processorFacade;
    private final GraphService graphService;

    public Graph visitLibrary(List<MethodStatement> publicMethods) {
        List<State> fixedMethodStates = new ArrayList<>(publicMethods.size());
        publicMethods.forEach(method -> {
            State methodProcessResult = processorFacade.process(method, State.EMPTY_STATE);
            fixedMethodStates.add(methodProcessResult);
        });

        return graphService.postProcess(fixedMethodStates);
    }
}
