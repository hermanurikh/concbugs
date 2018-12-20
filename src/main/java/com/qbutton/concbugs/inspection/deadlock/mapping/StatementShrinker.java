package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class StatementShrinker {

    @Nullable
    Statement shrinkStatements(List<Statement> statements) {
        if (statements.isEmpty()) {
            return null;
        }

        List<Statement> statementsToShrink = new ArrayList<>(statements);
        List<Statement> currentStatements = new ArrayList<>();

        while (statementsToShrink.size() != 1) {
            int currentSize = statementsToShrink.size();
            for (int i = 0; i < currentSize - 1; i += 2) {
                Statement first = statementsToShrink.get(i);
                Statement second = statementsToShrink.get(i + 1);

                Statement unionStatement = getStatement(first, second);
                if (unionStatement != null) {
                    currentStatements.add(unionStatement);
                }
            }
            if (currentSize % 2 == 1) {
                currentStatements.add(statementsToShrink.get(currentSize - 1));
            }
            statementsToShrink = new ArrayList<>(currentStatements);
            currentStatements.clear();
        }

        return statementsToShrink.get(0);
    }

    private Statement getStatement(Statement first, Statement second) {
        if (first != null && second != null) {
            return new SequentialStatement(first, second);
        }
        if (first == null) {
            if (second == null) {
                return null;
            }
            return second;
        } else {
            return first;
        }
    }
}
