package com.qbutton.concbugs.algorythm.service;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@DisplayName("SuperClassFinderService finds lowest superclass")
class SuperClassFinderServiceTest extends LightCodeInsightFixtureTestCase {

    private SuperClassFinderService superClassFinderService;

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
        superClassFinderService = new SuperClassFinderService(getProject());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @DisplayName("correctly when first and second have common non-object class")
    void findLowestSuperClass_commonSuperClass() {
        String lowestSuperClass = superClassFinderService.findLowestSuperClass("int", "java.lang.Double");

        assertThat(lowestSuperClass, is( "java.lang.Number"));
    }

    @Test
    @DisplayName("correctly when first is a subclass of second")
    void findLowestSuperClass_firstDerivableFromSecond() {
        String lowestSuperClass = superClassFinderService.findLowestSuperClass("int", "java.lang.Number");

        assertThat(lowestSuperClass, is( "java.lang.Number"));
    }

    @Test
    @DisplayName("correctly when second is a subclass of first")
    void findLowestSuperClass_secondDerivableFromFirst() {
        String lowestSuperClass = superClassFinderService.findLowestSuperClass("java.lang.Number", "int");

        assertThat(lowestSuperClass, is( "java.lang.Number"));
    }

    @Test
    @DisplayName("correctly when classes have only Object as superclass")
    void findLowestSuperClass_notDerivableClasses() {
        String lowestSuperClass = superClassFinderService.findLowestSuperClass("java.lang.String", "int");

        assertThat(lowestSuperClass, is( "java.lang.Object"));
    }

    @Test
    @DisplayName("correctly when classes are just the same")
    void findLowestSuperClass_sameClasses() {
        String lowestSuperClass = superClassFinderService.findLowestSuperClass("java.lang.String", "java.lang.String");

        assertThat(lowestSuperClass, is( "java.lang.String"));
    }
}