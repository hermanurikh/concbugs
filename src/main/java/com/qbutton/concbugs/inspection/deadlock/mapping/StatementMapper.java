package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiLoopStatement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

@RequiredArgsConstructor
@Setter
public class StatementMapper {

    private static final Logger LOGGER = Logger.getLogger(StatementMapper.class.getName());

    private StatementParser statementParser;

    BiConsumer<PsiStatement, List<Statement>> getParser(PsiStatement psiStatement) {
        if (psiStatement instanceof PsiDeclarationStatement) {
            return (statement, statements) ->
                    statementParser.parseDeclarationStatement((PsiDeclarationStatement) statement, statements);
        }

        if (psiStatement instanceof PsiExpressionStatement) {
            return (statement, statements) ->
                    statementParser.parseExpressionStatement((PsiExpressionStatement) statement, statements);
        }

        if (psiStatement instanceof PsiIfStatement) {
            return (statement, statements) ->
                    statementParser.parseIfStatement((PsiIfStatement) statement, statements);
        }

        if (psiStatement instanceof PsiSynchronizedStatement) {
            return (statement, statements) ->
                    statementParser.parseSynchronizedStatement((PsiSynchronizedStatement) statement, statements);
        }

        if (psiStatement instanceof PsiLoopStatement) {
            return (statement, statements) ->
                    statementParser.parseLoopStatement((PsiLoopStatement) statement, statements);
        }

        LOGGER.warning("no mapping found for statement " + psiStatement);

        return ((statement, statements) -> {});
    }
}
