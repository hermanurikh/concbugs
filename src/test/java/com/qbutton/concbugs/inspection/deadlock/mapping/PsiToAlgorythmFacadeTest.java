package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.qbutton.concbugs.algorythm.dto.MethodDeclaration;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.MethodStatement;
import com.qbutton.concbugs.algorythm.dto.statement.SequentialStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.fest.assertions.Assertions.assertThat;

@DisplayName("PsiToAlgorythmFacade")
class PsiToAlgorythmFacadeTest extends LightCodeInsightFixtureTestCase {

    private PsiToAlgorythmFacade psiToAlgorythmFacade;
    private MethodDeclaration test2MethodDeclaration;
    private MethodDeclaration getDateMethodDeclaration;

    PsiToAlgorythmFacadeTest() {
        StatementMapper statementMapper = new StatementMapper();
        StatementParser statementParser = new StatementParser(new StatementShrinker(), statementMapper);
        statementMapper.setStatementParser(statementParser);

        psiToAlgorythmFacade = new PsiToAlgorythmFacade(statementParser);

        initTest2MethodDeclaration();
        initGetDateMethodDeclaration();
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new ProjectDescriptor(LanguageLevel.HIGHEST) {
            @Override
            public Sdk getSdk() {
                return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
            }
        };
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/mapping/";
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Nested
    @DisplayName("parses declaration correctly")
    class ParseDeclarationStatements {

        @Test
        @DisplayName("when it is a simple declaration by type")
        void declarationByType_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Declaration_1.java");
            assertInstanceOf(readStatement, DeclarationStatement.class);
            DeclarationStatement result = (DeclarationStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(87);
            assertThat(result.getVarName()).isEqualTo("a");
            assertThat(result.getClazz()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("when it is a declaration initializer by new")
        void declarationByNewInitializer_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Declaration_2.java");
            assertInstanceOf(readStatement, DeclarationStatement.class);
            DeclarationStatement result = (DeclarationStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(70);
            assertThat(result.getVarName()).isEqualTo("b");
            assertThat(result.getClazz()).isEqualTo("java.lang.String");

        }
    }

    @Nested
    @DisplayName("parses cross assignment correctly")
    class ParseCrossAssignmentStatements {
        @Test
        @DisplayName("when it is a simple cross assignment")
        void crossAssignment_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("CrossAssignment_1.java");
            assertInstanceOf(readStatement, CrossAssignmentStatement.class);
            CrossAssignmentStatement result = (CrossAssignmentStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(73);
            assertThat(result.getVarName()).isEqualTo("b");
            assertThat(result.getRightValueName()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("parses inner assignment correctly")
    class ParseInnerAssignmentStatements {

        @Test
        @DisplayName("when it is a simple inner assignment")
        void innerAssignment_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("InnerAssignment_1.java");
            assertInstanceOf(readStatement, InnerAssignmentStatement.class);
            InnerAssignmentStatement result = (InnerAssignmentStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(99);
            assertThat(result.getVarName()).isEqualTo("b");
            assertThat(result.getClazz()).isEqualTo("java.util.Date");
        }

        @Test
        @DisplayName("when it is an inner assignment with declaration")
        void innerAssignment_withDeclaration_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("InnerAssignment_2.java");
            assertInstanceOf(readStatement, InnerAssignmentStatement.class);
            InnerAssignmentStatement innerAssignment = (InnerAssignmentStatement) readStatement;

            assertThat(innerAssignment.getLineNumber()).isEqualTo(106);
            assertThat(innerAssignment.getVarName()).isEqualTo("b");
            assertThat(innerAssignment.getClazz()).isEqualTo("java.util.Date");
        }
    }

    @Nested
    @DisplayName("parses branch statement correctly")
    class ParseBranchStatements {

        @Test
        @DisplayName("when it has both blocks")
        void branch_bothBlocks_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Branch_1.java");
            assertInstanceOf(readStatement, BranchStatement.class);
            BranchStatement result = (BranchStatement) readStatement;
            assertInstanceOf(result.getStmt1(), CrossAssignmentStatement.class);
            CrossAssignmentStatement stmt1 = (CrossAssignmentStatement) result.getStmt1();
            assertThat(stmt1.getVarName()).isEqualTo("a");
            assertThat(stmt1.getRightValueName()).isEqualTo("b");
            assertInstanceOf(result.getStmt2(), CrossAssignmentStatement.class);
            CrossAssignmentStatement stmt2 = (CrossAssignmentStatement) result.getStmt2();
            assertThat(stmt2.getVarName()).isEqualTo("c");
            assertThat(stmt2.getRightValueName()).isEqualTo("d");
        }

        @Test
        @DisplayName("when it has only if block")
        void branch_oneBlock_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Branch_2.java");
            assertInstanceOf(readStatement, BranchStatement.class);
            BranchStatement result = (BranchStatement) readStatement;
            assertInstanceOf(result.getStmt1(), CrossAssignmentStatement.class);
            CrossAssignmentStatement stmt1 = (CrossAssignmentStatement) result.getStmt1();
            assertThat(stmt1.getVarName()).isEqualTo("a");
            assertThat(stmt1.getRightValueName()).isEqualTo("b");
            assertThat(result.getStmt2()).isNull();
        }
    }

    @Nested
    @DisplayName("parses synchronized statement correctly")
    class ParseSynchronizedStatements {

        @Test
        @DisplayName("when it is a simple synchronized assignment")
        void synchronized_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Synchronized_1.java");
            assertInstanceOf(readStatement, SynchronizedStatement.class);
            SynchronizedStatement result = (SynchronizedStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(77);
            assertThat(result.getVarName()).isEqualTo("a");
            assertInstanceOf(result.getInnerStatement(), CrossAssignmentStatement.class);
            CrossAssignmentStatement stmt1 = (CrossAssignmentStatement) result.getInnerStatement();
            assertThat(stmt1.getVarName()).isEqualTo("b");
            assertThat(stmt1.getRightValueName()).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("parses wait statement correctly")
    class ParseWaitStatements {

        @Test
        @DisplayName("when it is a simple wait assignment")
        void wait_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Wait_1.java");
            assertInstanceOf(readStatement, WaitStatement.class);
            WaitStatement result = (WaitStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(98);
            assertThat(result.getVarName()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("parses method statement correctly")
    class ParseMethodStatements {

        @Test
        @DisplayName("when it is a simple method call with new var declaration")
        void method_varDeclaration_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Method_1.java");
            assertInstanceOf(readStatement, SequentialStatement.class);
            assertInstanceOf(((SequentialStatement) readStatement).getStmt1(), DeclarationStatement.class);
            assertInstanceOf(((SequentialStatement) readStatement).getStmt2(), MethodStatement.class);
            MethodStatement result = (MethodStatement) ((SequentialStatement) readStatement).getStmt2();
            assertThat(result.getLineNumber()).isEqualTo(119);
            assertThat(result.getVarName()).isEqualTo("b");
            assertThat(result.getReturnType()).isEqualTo("java.util.Date");
            assertThat(result.getMethodDeclarations().size()).isEqualTo(1);
            MethodDeclaration methodDeclaration = result.getMethodDeclarations().get(0);
            assertThat(methodDeclaration.getMethodName()).isEqualTo("getSomeDate");
            assertThat(methodDeclaration.getVariables().size()).isEqualTo(2);
            assertThat(methodDeclaration.getVariables().get(0).getVariableClass()).isEqualTo("Method_1");
            assertThat(methodDeclaration.getVariables().get(0).getVariableName()).isEqualTo("this");
            assertThat(methodDeclaration.getVariables().get(1).getVariableClass()).isEqualTo("java.lang.Object");
            assertThat(methodDeclaration.getVariables().get(1).getVariableName()).isEqualTo("expected");
            DeclarationStatement declarationStatement = (DeclarationStatement) ((SequentialStatement) readStatement).getStmt1();
            assertThat(declarationStatement.getLineNumber()).isEqualTo(115);
            assertThat(declarationStatement.getVarName()).isEqualTo("b");
            assertThat(declarationStatement.getClazz()).isEqualTo("java.util.Date");
        }

        @Test
        @DisplayName("when it is a simple method call with existing var initialization")
        void method_varInitialization_success() {
            Statement readStatement = readSingleStatementFromFirstMethod("Method_2.java");
            assertInstanceOf(readStatement, MethodStatement.class);
            MethodStatement result = (MethodStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(97);
            assertThat(result.getVarName()).isEqualTo("b");
            assertThat(result.getReturnType()).isEqualTo("java.util.Date");
            assertThat(result.getMethodDeclarations().size()).isEqualTo(1);
            MethodDeclaration methodDeclaration = result.getMethodDeclarations().get(0);
            assertThat(methodDeclaration.getMethodName()).isEqualTo("getSomeDate");
            assertThat(methodDeclaration.getVariables().size()).isEqualTo(2);
            assertThat(methodDeclaration.getVariables().get(0).getVariableClass()).isEqualTo("Method_2");
            assertThat(methodDeclaration.getVariables().get(0).getVariableName()).isEqualTo("this");
            assertThat(methodDeclaration.getVariables().get(1).getVariableClass()).isEqualTo("java.lang.Object");
            assertThat(methodDeclaration.getVariables().get(1).getVariableName()).isEqualTo("expected");
        }

        @Test
        @DisplayName("when it is a method call and there is an overriding method")
        void method_overridenMethodExists() {
            Statement readStatement = readSingleStatementFromFirstMethod("Method_3.java");
            assertInstanceOf(readStatement, MethodStatement.class);
            MethodStatement result = (MethodStatement) readStatement;
            assertThat(result.getReturnType()).isEqualTo("void");
            assertThat(result.getMethodDeclarations().size()).isEqualTo(2);

            MethodDeclaration first = result.getMethodDeclarations().get(0);
            assertThat(first.getMethodName()).isEqualTo("doSomething");
            assertThat(first.getVariables().size()).isEqualTo(2);
            assertThat(first.getVariables().get(0).getVariableClass()).isEqualTo("mapping.Method_3");
            assertThat(first.getVariables().get(0).getVariableName()).isEqualTo("this");
            assertThat(first.getVariables().get(1).getVariableClass()).isEqualTo("java.lang.String");
            assertThat(first.getVariables().get(1).getVariableName()).isEqualTo("expected");

            MethodDeclaration second = result.getMethodDeclarations().get(1);
            assertThat(second.getMethodName()).isEqualTo("doSomething");
            assertThat(second.getVariables().size()).isEqualTo(2);
            assertThat(second.getVariables().get(0).getVariableClass()).isEqualTo("mapping.Method_3.InnerClass");
            assertThat(second.getVariables().get(0).getVariableName()).isEqualTo("this");
            assertThat(second.getVariables().get(1).getVariableClass()).isEqualTo("java.lang.String");
            assertThat(second.getVariables().get(1).getVariableName()).isEqualTo("expectedOverriden");
        }
    }

    @Nested
    @DisplayName("parses loop statement correctly")
    class ParseLoopStatements {

        @Test
        @DisplayName("when there are different loops")
        void loop_success() {
            Statement statement = readSingleStatementFromFirstMethod("Loop_1.java");
            assertInstanceOf(statement, SequentialStatement.class);
            assertInstanceOf(((SequentialStatement) statement).getStmt1(), SequentialStatement.class);
            assertInstanceOf(((SequentialStatement) statement).getStmt2(), SequentialStatement.class);
            SequentialStatement firstSequentialStatement = (SequentialStatement) ((SequentialStatement) statement).getStmt1();
            SequentialStatement secondSequentialStatement = (SequentialStatement) ((SequentialStatement) statement).getStmt2();

            assertInstanceOf(firstSequentialStatement.getStmt1(), CrossAssignmentStatement.class);
            assertThat(((CrossAssignmentStatement) firstSequentialStatement.getStmt1()).getRightValueName()).isEqualTo("b");
            assertThat(firstSequentialStatement.getStmt1().getVarName()).isEqualTo("a");

            assertInstanceOf(firstSequentialStatement.getStmt2(), CrossAssignmentStatement.class);
            assertThat(((CrossAssignmentStatement) firstSequentialStatement.getStmt2()).getRightValueName()).isEqualTo("d");
            assertThat(firstSequentialStatement.getStmt2().getVarName()).isEqualTo("c");

            assertInstanceOf(secondSequentialStatement.getStmt1(), CrossAssignmentStatement.class);
            assertThat(((CrossAssignmentStatement) secondSequentialStatement.getStmt1()).getRightValueName()).isEqualTo("f");
            assertThat(secondSequentialStatement.getStmt1().getVarName()).isEqualTo("e");

            assertInstanceOf(secondSequentialStatement.getStmt2(), CrossAssignmentStatement.class);
            assertThat(((CrossAssignmentStatement) secondSequentialStatement.getStmt2()).getRightValueName()).isEqualTo("h");
            assertThat(secondSequentialStatement.getStmt2().getVarName()).isEqualTo("g");
        }
    }

    @Nested
    @DisplayName("parses complex method correctly")
    class ParseComplexMethod {

        @Test
        @DisplayName("when there are many different statements in it")
        void manyStatements_success() {
            Statement statement = readSingleStatementFromFirstMethod("Complex.java");
            assertInstanceOf(statement, SequentialStatement.class);
            List<Statement> actualStatements = new ArrayList<>();
            demapSequentialStatement((SequentialStatement) statement, actualStatements);

            assertThat(actualStatements.size()).isEqualTo(21);

            assertInstanceOf(actualStatements.get(0), DeclarationStatement.class);
            assertThat(actualStatements.get(0).getVarName()).isEqualTo("a");
            assertThat(actualStatements.get(0).getLineNumber()).isEqualTo(130);
            assertThat(((DeclarationStatement) actualStatements.get(0)).getClazz()).isEqualTo("Complex");

            assertInstanceOf(actualStatements.get(1), DeclarationStatement.class);
            assertThat(actualStatements.get(1).getVarName()).isEqualTo("a");
            assertThat(actualStatements.get(1).getLineNumber()).isEqualTo(155);
            assertThat(((DeclarationStatement) actualStatements.get(1)).getClazz()).isEqualTo("Complex");

            assertInstanceOf(actualStatements.get(2), DeclarationStatement.class);
            assertThat(actualStatements.get(2).getVarName()).isEqualTo("b");
            assertThat(actualStatements.get(2).getLineNumber()).isEqualTo(204);
            assertThat(((DeclarationStatement) actualStatements.get(2)).getClazz()).isEqualTo("Complex");

            assertInstanceOf(actualStatements.get(3), CrossAssignmentStatement.class);
            assertThat(actualStatements.get(3).getVarName()).isEqualTo("b");
            assertThat(actualStatements.get(3).getLineNumber()).isEqualTo(245);
            assertThat(((CrossAssignmentStatement) actualStatements.get(3)).getRightValueName()).isEqualTo("a");

            assertInstanceOf(actualStatements.get(4), DeclarationStatement.class);
            assertThat(actualStatements.get(4).getVarName()).isEqualTo("a");
            assertThat(actualStatements.get(4).getLineNumber()).isEqualTo(363);
            assertThat(((DeclarationStatement) actualStatements.get(4)).getClazz()).isEqualTo("Complex.ComplexImpl");

            assertInstanceOf(actualStatements.get(5), CrossAssignmentStatement.class);
            assertThat(actualStatements.get(5).getVarName()).isEqualTo("b");
            assertThat(actualStatements.get(5).getLineNumber()).isEqualTo(359);
            assertThat(((CrossAssignmentStatement) actualStatements.get(5)).getRightValueName()).isEqualTo("a");

            assertInstanceOf(actualStatements.get(6), CrossAssignmentStatement.class);
            assertThat(actualStatements.get(6).getVarName()).isEqualTo("a");
            assertThat(actualStatements.get(6).getLineNumber()).isEqualTo(355);
            assertThat(((CrossAssignmentStatement) actualStatements.get(6)).getRightValueName()).isEqualTo("b");

            assertInstanceOf(actualStatements.get(7), InnerAssignmentStatement.class);
            assertThat(actualStatements.get(7).getVarName()).isEqualTo("c");
            assertThat(actualStatements.get(7).getLineNumber()).isEqualTo(425);
            assertThat(((InnerAssignmentStatement) actualStatements.get(7)).getClazz()).isEqualTo("int");

            assertInstanceOf(actualStatements.get(8), InnerAssignmentStatement.class);
            assertThat(actualStatements.get(8).getVarName()).isEqualTo("c");
            assertThat(actualStatements.get(8).getLineNumber()).isEqualTo(475);
            assertThat(((InnerAssignmentStatement) actualStatements.get(8)).getClazz()).isEqualTo("int");

            assertInstanceOf(actualStatements.get(9), BranchStatement.class);
            assertThat(actualStatements.get(9).getLineNumber()).isEqualTo(550);
            assertInstanceOf(((BranchStatement) actualStatements.get(9)).getStmt1(), MethodStatement.class);
            assertInstanceOf(((BranchStatement) actualStatements.get(9)).getStmt2(), MethodStatement.class);
            MethodStatement branchMethodStatement1 = (MethodStatement) ((BranchStatement) actualStatements.get(9)).getStmt1();
            MethodStatement branchMethodStatement2 = (MethodStatement) ((BranchStatement) actualStatements.get(9)).getStmt2();
            assertThat(branchMethodStatement1.getReturnType()).isEqualTo("Date");
            assertThat(branchMethodStatement2.getReturnType()).isEqualTo("Date");
            assertThat(branchMethodStatement1.getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);
            assertThat(branchMethodStatement2.getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);

            assertInstanceOf(actualStatements.get(10), BranchStatement.class);
            assertThat(actualStatements.get(10).getLineNumber()).isEqualTo(721);
            assertInstanceOf(((BranchStatement) actualStatements.get(10)).getStmt1(), MethodStatement.class);
            assertNull(((BranchStatement) actualStatements.get(10)).getStmt2());
            MethodStatement newBranchMethodStatement = (MethodStatement) ((BranchStatement) actualStatements.get(10)).getStmt1();
            assertThat(newBranchMethodStatement.getReturnType()).isEqualTo("Date");
            assertThat(newBranchMethodStatement.getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);

            assertInstanceOf(actualStatements.get(11), BranchStatement.class);
            assertThat(actualStatements.get(11).getLineNumber()).isEqualTo(834);
            assertInstanceOf(((BranchStatement) actualStatements.get(11)).getStmt1(), MethodStatement.class);
            assertNull(((BranchStatement) actualStatements.get(11)).getStmt2());
            MethodStatement newBranchMethodStatement2 = (MethodStatement) ((BranchStatement) actualStatements.get(11)).getStmt1();
            assertThat(newBranchMethodStatement2.getReturnType()).isEqualTo("Date");
            assertThat(newBranchMethodStatement2.getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);

            assertInstanceOf(actualStatements.get(12), SynchronizedStatement.class);
            assertThat(actualStatements.get(12).getLineNumber()).isEqualTo(969);
            assertThat(actualStatements.get(12).getVarName()).isEqualTo("a");
            assertInstanceOf(((SynchronizedStatement) actualStatements.get(12)).getInnerStatement(), MethodStatement.class);
            MethodStatement newSynchronizedMethodStatement = (MethodStatement) ((SynchronizedStatement) actualStatements.get(12)).getInnerStatement();
            assertThat(newSynchronizedMethodStatement.getReturnType()).isEqualTo("Date");
            assertThat(newSynchronizedMethodStatement.getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);

            assertInstanceOf(actualStatements.get(13), MethodStatement.class);
            assertThat(actualStatements.get(13).getVarName()).isEqualTo("other");
            assertThat(actualStatements.get(13).getLineNumber()).isEqualTo(1154);
            assertThat(((MethodStatement) actualStatements.get(13)).getReturnType()).isEqualTo("Date");
            assertThat(((MethodStatement) actualStatements.get(13)).getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);

            assertInstanceOf(actualStatements.get(14), DeclarationStatement.class);
            assertThat(actualStatements.get(14).getVarName()).isEqualTo("newDate");
            assertThat(actualStatements.get(14).getLineNumber()).isEqualTo(1247);
            assertThat(((DeclarationStatement) actualStatements.get(14)).getClazz()).isEqualTo("Date");

            assertInstanceOf(actualStatements.get(15), MethodStatement.class);
            assertThat(actualStatements.get(15).getVarName()).isEqualTo("newDate");
            assertThat(actualStatements.get(15).getLineNumber()).isEqualTo(1257);
            assertThat(((MethodStatement) actualStatements.get(15)).getReturnType()).isEqualTo("Date");
            assertThat(((MethodStatement) actualStatements.get(15)).getMethodDeclarations().get(0)).isEqualTo(test2MethodDeclaration);

            assertInstanceOf(actualStatements.get(16), WaitStatement.class);
            assertThat(actualStatements.get(16).getLineNumber()).isEqualTo(1310);
            assertThat(actualStatements.get(16).getVarName()).isEqualTo("a");

            assertInstanceOf(actualStatements.get(17), DeclarationStatement.class);
            assertThat(actualStatements.get(17).getVarName()).isEqualTo("ab");
            assertThat(actualStatements.get(17).getLineNumber()).isEqualTo(1369);
            assertThat(((DeclarationStatement) actualStatements.get(17)).getClazz()).isEqualTo("int");

            assertInstanceOf(actualStatements.get(18), DeclarationStatement.class);
            assertThat(actualStatements.get(18).getVarName()).isEqualTo("ac");
            assertThat(actualStatements.get(18).getLineNumber()).isEqualTo(1373);
            assertThat(((DeclarationStatement) actualStatements.get(18)).getClazz()).isEqualTo("int");

            assertInstanceOf(actualStatements.get(19), DeclarationStatement.class);
            assertThat(actualStatements.get(19).getVarName()).isEqualTo("ad");
            assertThat(actualStatements.get(19).getLineNumber()).isEqualTo(1377);
            assertThat(((DeclarationStatement) actualStatements.get(19)).getClazz()).isEqualTo("int");

            assertInstanceOf(actualStatements.get(20), MethodStatement.class);
            assertThat(actualStatements.get(20).getVarName()).isEqualTo("newDate");
            assertThat(actualStatements.get(20).getLineNumber()).isEqualTo(1751);
            assertThat(((MethodStatement) actualStatements.get(20)).getReturnType()).isEqualTo("Date");
            assertThat(((MethodStatement) actualStatements.get(20)).getMethodDeclarations().get(0)).isEqualTo(getDateMethodDeclaration);
        }

    }

    private Statement readSingleStatementFromFirstMethod(String fileName) {

        try {
            PsiFile[] psiFiles = myFixture.configureByFiles(fileName);
            AtomicReference<Statement> statement = new AtomicReference<>();

            ApplicationManagerEx.getApplicationEx().runReadAction(() -> {
                PsiJavaFile file = (PsiJavaFile) psiFiles[0];
                PsiClass clazz = file.getClasses()[0];
                PsiMethod analyzedMethod = clazz.getMethods()[0];
                MethodStatement parsedMethodStatement = psiToAlgorythmFacade.parseMethod(analyzedMethod);
                statement.set(parsedMethodStatement.getMethodDeclarations().get(0).getMethodBody());
            });

            return statement.get();
        } catch (Exception ex) {
            fail(ex.getMessage());
            return null;
        }
    }

    private void demapSequentialStatement(SequentialStatement sequentialStatement, List<Statement> statements) {
        if (sequentialStatement.getStmt1() instanceof SequentialStatement) {
            demapSequentialStatement((SequentialStatement) sequentialStatement.getStmt1(), statements);
        } else {
            statements.add(sequentialStatement.getStmt1());
        }

        if (sequentialStatement.getStmt2() instanceof SequentialStatement) {
            demapSequentialStatement((SequentialStatement) sequentialStatement.getStmt2(), statements);
        } else {
            statements.add(sequentialStatement.getStmt2());
        }
    }

    private void initTest2MethodDeclaration() {
        List<MethodDeclaration.Variable> variables = ImmutableList.of(
                new MethodDeclaration.Variable("Complex.class", "Complex"),
                new MethodDeclaration.Variable("a", "java.lang.String"),
                new MethodDeclaration.Variable("b", "Date")
        );
        test2MethodDeclaration = new MethodDeclaration("test2", variables,
                new CrossAssignmentStatement(1837, "c", "d"));
    }

    private void initGetDateMethodDeclaration() {
        List<MethodDeclaration.Variable> variables = ImmutableList.of(
                new MethodDeclaration.Variable("this", "Complex")
        );
        getDateMethodDeclaration = new MethodDeclaration("getDate", variables,
                new SynchronizedStatement(1751, "this",
                        new CrossAssignmentStatement(1922, "a", "b")));
    }
}