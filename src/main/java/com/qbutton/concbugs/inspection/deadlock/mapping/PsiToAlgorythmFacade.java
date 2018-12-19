package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class PsiToAlgorythmFacade {

    private final StatementMapper statementMapper;

    public Statement parseStatements(PsiCodeBlock psiCodeBlock) {
        PsiStatement[] statements = psiCodeBlock.getStatements();
        List<Statement> resultStatements = new ArrayList<>();

        Arrays.stream(statements)
                .forEach(psiStatement -> {
                    Statement statement = parseStatement(psiStatement);
                    resultStatements.add(statement);
                });


        return shrinkStatements(resultStatements);
    }

    public Statement parseStatement(PsiStatement statement) {
        List<Statement> resultStatements = new ArrayList<>();

        BiConsumer<PsiStatement, List<Statement>> parseFunction = statementMapper.getParser(statement);
        parseFunction.accept(statement, resultStatements);

        return shrinkStatements(resultStatements);
    }

    private Statement shrinkStatements(List<Statement> statements) {
        //todo
        return statements.get(0);
    }

}
