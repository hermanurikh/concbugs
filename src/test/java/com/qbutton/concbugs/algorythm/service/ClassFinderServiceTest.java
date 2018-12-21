package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@DisplayName("ClassFinderService")
class ClassFinderServiceTest extends LightCodeInsightFixtureTestCase {

    private ClassFinderService classFinderService;

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
        classFinderService = new ClassFinderService(getProject());
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Nested
    @DisplayName("finds lowest superclass")
    class FindLowestSuperClass {

        @Test
        @DisplayName("correctly when first and second have common non-object class")
        void findLowestSuperClass_commonSuperClass() {
            String lowestSuperClass = classFinderService.findLowestSuperClass("int", "java.lang.Double");

            assertThat(lowestSuperClass, is( "java.lang.Number"));
        }

        @Test
        @DisplayName("correctly when first is a subclass of second")
        void findLowestSuperClass_firstDerivableFromSecond() {
            String lowestSuperClass = classFinderService.findLowestSuperClass("int", "java.lang.Number");

            assertThat(lowestSuperClass, is( "java.lang.Number"));
        }

        @Test
        @DisplayName("correctly when second is a subclass of first")
        void findLowestSuperClass_secondDerivableFromFirst() {
            String lowestSuperClass = classFinderService.findLowestSuperClass("java.lang.Number", "int");

            assertThat(lowestSuperClass, is( "java.lang.Number"));
        }

        @Test
        @DisplayName("correctly when classes have only Object as superclass")
        void findLowestSuperClass_notDerivableClasses() {
            String lowestSuperClass = classFinderService.findLowestSuperClass("java.lang.String", "int");

            assertThat(lowestSuperClass, is( "java.lang.Object"));
        }

        @Test
        @DisplayName("correctly when classes are just the same")
        void findLowestSuperClass_sameClasses() {
            String lowestSuperClass = classFinderService.findLowestSuperClass("java.lang.String", "java.lang.String");

            assertThat(lowestSuperClass, is( "java.lang.String"));
        }
    }

    @Nested
    @DisplayName("finds all subclasses")
    class FindAllSubclasses {

        @Test
        @DisplayName("correctly for java.lang.Number")
        void forNumber() {
            Set<String> subclasses = classFinderService.getSubclassesOf("java.lang.Number");

            assertContainsElements(subclasses, ImmutableSet.of(
                    "java.lang.Byte",
                    "java.lang.Double",
                    "java.lang.Float",
                    "java.lang.Integer",
                    "java.lang.Long",
                    "java.lang.Number",
                    "java.lang.Short"

            ));

        }
    }
}