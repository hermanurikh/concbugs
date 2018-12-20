package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.intellij.psi.PsiMethod;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PsiToAlgorythmFacade {

    private final StatementParser statementParser;

    public MethodStatement parseMethod(PsiMethod psiMethod) {
        return statementParser.parseMethod(psiMethod);
    }
}
