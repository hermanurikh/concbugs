package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.ResolveTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.qbutton.concbugs.algorythm.dto.statement.BranchStatement;
import com.qbutton.concbugs.algorythm.dto.statement.CrossAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.DeclarationStatement;
import com.qbutton.concbugs.algorythm.dto.statement.InnerAssignmentStatement;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import com.qbutton.concbugs.algorythm.dto.statement.SynchronizedStatement;
import com.qbutton.concbugs.algorythm.dto.statement.WaitStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.fest.assertions.Assertions.assertThat;

@DisplayName("PsiToAlgorythmFacade")
class PsiToAlgorythmFacadeTest extends ResolveTestCase {

    private PsiToAlgorythmFacade psiToAlgorythmFacade;

    private CodeInsightTestFixture testFixture;


    @BeforeEach
    void init() throws Exception {
        StatementParser statementParser = new StatementParser();
        StatementMapper statementMapper = new StatementMapper(statementParser);
        psiToAlgorythmFacade = new PsiToAlgorythmFacade(statementMapper);

        statementParser.setPsiToAlgorythmFacade(psiToAlgorythmFacade);

        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                factory.createLightFixtureBuilder(LightCodeInsightFixtureTestCase.JAVA_8);
        final IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        testFixture = factory.createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));
        //testFixture = factory.createCodeInsightFixture(fixture, new HeavyTest(true));
        testFixture.setTestDataPath("src/test/resources/mapping/");
        testFixture.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/mapping/";
    }



    @AfterEach
    public void tearDown() throws Exception {
        testFixture.tearDown();
    }

    @Nested
    @DisplayName("parses declaration correctly")
    class ParseDeclarationStatements {

        @Test
        @DisplayName("when it is a simple declaration by type")
        void declarationByType_success() {
            Statement readStatement = readSingleStatement("Declaration_1.java");
            assertTrue(readStatement instanceof DeclarationStatement);
            DeclarationStatement result = (DeclarationStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(87);
            assertThat(result.getVarName()).isEqualTo("a");
            assertThat(result.getClazz()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("when it is a declaration initializer by new")
        void declarationByNewInitializer_success() {
            Statement readStatement = readSingleStatement("Declaration_2.java");
            assertTrue(readStatement instanceof DeclarationStatement);
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
            Statement readStatement = readSingleStatement("CrossAssignment_1.java");
            assertTrue(readStatement instanceof CrossAssignmentStatement);
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
            Statement readStatement = readSingleStatement("InnerAssignment_1.java");
            assertTrue(readStatement instanceof InnerAssignmentStatement);
            InnerAssignmentStatement result = (InnerAssignmentStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(99);
            assertThat(result.getVarName()).isEqualTo("b");
            assertThat(result.getClazz()).isEqualTo("java.util.Date");
        }
    }

    @Nested
    @DisplayName("parses branch statement correctly")
    class ParseBranchStatements {

        @Test
        @DisplayName("when it has both blocks")
        void branch_bothBlocks_success() {
            Statement readStatement = readSingleStatement("Branch_1.java");
            assertTrue(readStatement instanceof BranchStatement);
            BranchStatement result = (BranchStatement) readStatement;
            assertThat(result.getStmt1() instanceof CrossAssignmentStatement);
            CrossAssignmentStatement stmt1 = (CrossAssignmentStatement) result.getStmt1();
            assertThat(stmt1.getVarName()).isEqualTo("a");
            assertThat(stmt1.getRightValueName()).isEqualTo("b");
            assertThat(result.getStmt2() instanceof CrossAssignmentStatement);
            CrossAssignmentStatement stmt2 = (CrossAssignmentStatement) result.getStmt2();
            assertThat(stmt2.getVarName()).isEqualTo("c");
            assertThat(stmt2.getRightValueName()).isEqualTo("d");
        }

        @Test
        @DisplayName("when it has only if block")
        void branch_oneBlock_success() {
            Statement readStatement = readSingleStatement("Branch_2.java");
            assertTrue(readStatement instanceof BranchStatement);
            BranchStatement result = (BranchStatement) readStatement;
            assertThat(result.getStmt1() instanceof CrossAssignmentStatement);
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
            Statement readStatement = readSingleStatement("Synchronized_1.java");
            assertTrue(readStatement instanceof SynchronizedStatement);
            SynchronizedStatement result = (SynchronizedStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(77);
            assertThat(result.getVarName()).isEqualTo("a");
            assertThat(result.getInnerStatement() instanceof CrossAssignmentStatement);
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
            Statement readStatement = readSingleStatement("Wait_1.java");
            assertTrue(readStatement instanceof WaitStatement);
            WaitStatement result = (WaitStatement) readStatement;
            assertThat(result.getLineNumber()).isEqualTo(77);
            assertThat(result.getVarName()).isEqualTo("a");
        }
    }

    private Statement readSingleStatement(String fileName) {

        try {
            PsiFile[] psiFiles = testFixture.configureByFiles(fileName);
            //PsiReference psiReference =  configureByFile(fileName);
            AtomicReference<Statement> statement = new AtomicReference<>();

            ApplicationManagerEx.getApplicationEx().runReadAction(() -> {
                PsiJavaFile file = (PsiJavaFile) psiFiles[0];
                //PsiJavaFile file = (PsiJavaFile) psiReference;
                PsiClass clazz = file.getClasses()[0];
                PsiMethod analyzedMethod = clazz.getMethods()[0];
                statement.set(psiToAlgorythmFacade.parseStatements(analyzedMethod.getBody()));
            });

            return statement.get();
        } catch (Exception ex) {
            return null;
        }

    }

   /* @Override
    protected Sdk getTestProjectJdk() {
        //return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
        return JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
    }*/


}