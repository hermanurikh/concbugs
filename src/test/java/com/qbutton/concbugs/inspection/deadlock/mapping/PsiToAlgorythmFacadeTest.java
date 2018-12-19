package com.qbutton.concbugs.inspection.deadlock.mapping;

import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.qbutton.concbugs.algorythm.dto.statement.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.LightPlatformTestCase.getProject;

@DisplayName("PsiToAlgorythmFacade")
class PsiToAlgorythmFacadeTest extends UsefulTestCase {

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
        testFixture.setTestDataPath("src/test/resources/mapping/");
        testFixture.setUp();
    }

    @Nested
    @DisplayName("parses code block correcly")
    class ParseStatements {

        @Test
        @DisplayName("when it is a simple declaration")
        void declaration_success() {
            PsiFileFactory instance = PsiFileFactory.getInstance(getProject());
            PsiFile[] psiFiles = testFixture.configureByFiles("DeclarationSuccess.java");

            final ApplicationEx application = ApplicationManagerEx.getApplicationEx();

            application.runReadAction(() -> {

                PsiJavaFile file = (PsiJavaFile) psiFiles[0];
                PsiClass clazz = file.getClasses()[0];
                PsiMethod analyzedMethod = clazz.getMethods()[0];
                Statement statement = psiToAlgorythmFacade.parseStatements(analyzedMethod.getBody());
                System.out.println();
            });
            //FilenameIndex.getFilesByName("")
        }

    }
}