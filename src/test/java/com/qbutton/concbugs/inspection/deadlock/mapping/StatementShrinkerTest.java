package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.google.common.collect.ImmutableList;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("StatementShrinker")
class StatementShrinkerTest {

    private final StatementShrinker statementShrinker = new StatementShrinker();

    @Test
    @DisplayName("shrinks correctly when there are 3 of them")
    void shrink_threeItems_success() {
        Statement statement1 = new WaitStatement(2, "1");
        Statement statement2 = new WaitStatement(3, "2");
        Statement statement3 = new WaitStatement(4, "3");

        Statement statement = statementShrinker.shrinkStatements(ImmutableList.of(statement1, statement2, statement3));

        assertThat(statement instanceof SequentialStatement);
        SequentialStatement sequentialStatement1 = (SequentialStatement) statement;
        assertThat(sequentialStatement1.getStmt1() instanceof SequentialStatement);
        assertThat(sequentialStatement1.getStmt2() instanceof WaitStatement);
        SequentialStatement sequentialStatement2 = (SequentialStatement) sequentialStatement1.getStmt1();
        assertThat(sequentialStatement2.getStmt1() instanceof WaitStatement);
        assertThat(sequentialStatement2.getStmt2() instanceof WaitStatement);
        assertThat(sequentialStatement2.getStmt1().getOffset()).isEqualTo(2);
        assertThat(sequentialStatement2.getStmt2().getOffset()).isEqualTo(3);
        assertThat(sequentialStatement1.getStmt2().getOffset()).isEqualTo(4);
    }

    @Test
    @DisplayName("shrinks correctly when there is 1 of them")
    void shrink_oneItem_success() {
        Statement statement1 = new WaitStatement(2, "1");

        Statement statement = statementShrinker.shrinkStatements(ImmutableList.of(statement1));

        assertThat(statement instanceof WaitStatement);
        WaitStatement waitStatement = (WaitStatement) statement;
        assertThat(waitStatement.getOffset()).isEqualTo(2);
        assertThat(waitStatement.getVarName()).isEqualTo("1");
    }

    @Test
    @DisplayName("shrinks correctly when there are 2 of them")
    void shrink_twoItems_success() {
        Statement statement1 = new WaitStatement(2, "1");
        Statement statement2 = new WaitStatement(3, "2");

        Statement statement = statementShrinker.shrinkStatements(ImmutableList.of(statement1, statement2));

        assertThat(statement instanceof SequentialStatement);
        SequentialStatement sequentialStatement = (SequentialStatement) statement;
        assertThat(sequentialStatement.getStmt1() instanceof WaitStatement);
        assertThat(sequentialStatement.getStmt2() instanceof WaitStatement);
        assertThat(sequentialStatement.getStmt1().getOffset()).isEqualTo(2);
        assertThat(sequentialStatement.getStmt1().getVarName()).isEqualTo("1");
        assertThat(sequentialStatement.getStmt2().getOffset()).isEqualTo(3);
        assertThat(sequentialStatement.getStmt2().getVarName()).isEqualTo("2");
    }

    @Test
    @DisplayName("shrinks correctly when there are 5 of them")
    void shrink_fiveItems_success() {
        Statement statement1 = new WaitStatement(2, "1");
        Statement statement2 = new WaitStatement(3, "2");
        Statement statement3 = new WaitStatement(4, "3");
        Statement statement4 = new WaitStatement(5, "4");
        Statement statement5 = new WaitStatement(6, "5");

        Statement statement = statementShrinker.shrinkStatements(ImmutableList.of(
                statement1, statement2, statement3, statement4, statement5));

        assertThat(statement instanceof SequentialStatement);
        SequentialStatement sequentialStatement1 = (SequentialStatement) statement;
        assertThat(sequentialStatement1.getStmt1() instanceof SequentialStatement);
        assertThat(sequentialStatement1.getStmt2() instanceof WaitStatement);
        SequentialStatement sequentialStatement2 = (SequentialStatement) sequentialStatement1.getStmt1();
        assertThat(sequentialStatement2.getStmt1() instanceof SequentialStatement);
        assertThat(sequentialStatement2.getStmt2() instanceof SequentialStatement);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt1()).getStmt1() instanceof WaitStatement);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt1()).getStmt1().getOffset()).isEqualTo(2);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt1()).getStmt1().getVarName()).isEqualTo("1");
        assertThat(((SequentialStatement) sequentialStatement2.getStmt1()).getStmt2() instanceof WaitStatement);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt1()).getStmt2().getOffset()).isEqualTo(3);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt1()).getStmt2().getVarName()).isEqualTo("2");
        assertThat(((SequentialStatement) sequentialStatement2.getStmt2()).getStmt1() instanceof WaitStatement);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt2()).getStmt1().getOffset()).isEqualTo(4);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt2()).getStmt1().getVarName()).isEqualTo("3");
        assertThat(((SequentialStatement) sequentialStatement2.getStmt2()).getStmt2() instanceof WaitStatement);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt2()).getStmt2().getOffset()).isEqualTo(5);
        assertThat(((SequentialStatement) sequentialStatement2.getStmt2()).getStmt2().getVarName()).isEqualTo("4");
        assertThat(sequentialStatement1.getStmt2().getOffset()).isEqualTo(6);
        assertThat(sequentialStatement1.getStmt2().getVarName()).isEqualTo("5");
    }

    @Test
    @DisplayName("shrinks correctly when there are 4 of them")
    void shrink_fourItems_success() {
        Statement statement1 = new WaitStatement(2, "1");
        Statement statement2 = new WaitStatement(3, "2");
        Statement statement3 = new WaitStatement(4, "3");
        Statement statement4 = new WaitStatement(5, "4");

        Statement statement = statementShrinker.shrinkStatements(ImmutableList.of(
                statement1, statement2, statement3, statement4));

        assertThat(statement instanceof SequentialStatement);
        SequentialStatement sequentialStatement1 = (SequentialStatement) statement;
        assertThat(sequentialStatement1.getStmt1() instanceof SequentialStatement);
        assertThat(sequentialStatement1.getStmt2() instanceof SequentialStatement);
        SequentialStatement sequentialStatement2 = (SequentialStatement) sequentialStatement1.getStmt1();
        SequentialStatement sequentialStatement3 = (SequentialStatement) sequentialStatement1.getStmt2();
        assertThat(sequentialStatement2.getStmt1() instanceof WaitStatement);
        assertThat(sequentialStatement2.getStmt2() instanceof WaitStatement);
        assertThat(sequentialStatement3.getStmt1() instanceof WaitStatement);
        assertThat(sequentialStatement3.getStmt2() instanceof WaitStatement);

        assertThat(sequentialStatement2.getStmt1().getOffset()).isEqualTo(2);
        assertThat(sequentialStatement2.getStmt1().getVarName()).isEqualTo("1");
        assertThat(sequentialStatement2.getStmt2().getOffset()).isEqualTo(3);
        assertThat(sequentialStatement2.getStmt2().getVarName()).isEqualTo("2");
        assertThat(sequentialStatement3.getStmt1().getOffset()).isEqualTo(4);
        assertThat(sequentialStatement3.getStmt1().getVarName()).isEqualTo("3");
        assertThat(sequentialStatement3.getStmt2().getOffset()).isEqualTo(5);
        assertThat(sequentialStatement3.getStmt2().getVarName()).isEqualTo("4");
    }

    @Test
    @DisplayName("shrinks correctly when there are 0 of them")
    void shrink_noItems_success() {

        Statement statement = statementShrinker.shrinkStatements(Collections.emptyList());

        assertNull(statement);
    }

    @Test
    @DisplayName("shrinks correctly when first statement is null")
    void shrink_firstNullStatement_success() {
        Statement statement1 = new WaitStatement(2, "1");
        List<Statement> statements = new ArrayList<>(2);
        statements.add(null);
        statements.add(statement1);

        Statement statement = statementShrinker.shrinkStatements(statements);

        assertThat(statement).isEqualTo(statement1);
    }

    @Test
    @DisplayName("shrinks correctly when second statement is null")
    void shrink_secondNullStatement_success() {
        Statement statement1 = new WaitStatement(2, "1");
        List<Statement> statements = new ArrayList<>(2);
        statements.add(statement1);
        statements.add(null);

        Statement statement = statementShrinker.shrinkStatements(statements);

        assertThat(statement).isEqualTo(statement1);
    }

    @Test
    @DisplayName("shrinks correctly when both statements are null")
    void shrink_bothNullStatements_success() {
        List<Statement> statements = new ArrayList<>(2);
        statements.add(null);
        statements.add(null);

        Statement statement = statementShrinker.shrinkStatements(statements);

        assertNull(statement);
    }
}