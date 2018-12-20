package com.qbutton.concbugs.algorythm.service;

import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;

import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
class SuperClassFinderService {

    private final Project project;

    String findLowestSuperClass(String class1, String class2) {

        AtomicReference<String> realClass1 = new AtomicReference<>(class1);
        AtomicReference<String> realClass2 = new AtomicReference<>(class2);

        AtomicReference<String> commonSuperClass = new AtomicReference<>();

        ApplicationManagerEx.getApplicationEx().runReadAction(() -> {
            checkAndUpgradeToWrapper(class1, realClass1);
            checkAndUpgradeToWrapper(class2, realClass2);

            PsiClass firstClass = getClassFromString(realClass1);
            PsiClass secondClass = getClassFromString(realClass2);

            PsiClass result = findLowestSuperClass(firstClass, secondClass);
            commonSuperClass.set(result.getQualifiedName());
        });

        return commonSuperClass.get();
    }

    private PsiClass getClassFromString(AtomicReference<String> stringClass) {
        return JavaPsiFacade.getInstance(project).findClass(stringClass.get(), GlobalSearchScope.allScope(project));
    }

    private void checkAndUpgradeToWrapper(String stringClass, AtomicReference<String> realClass) {
        if (isPrimitive(stringClass)) {
            realClass.set(ClassUtils.primitiveToWrapper(getClassSafe(stringClass)).getCanonicalName());
        }
    }

    private boolean isPrimitive(String className) {
        try {
            Class<?> aClass = ClassUtils.getClass(className);
            return aClass.isPrimitive();
        } catch (ClassNotFoundException e) {
            //expected for cases when we don't have this class in plugin classloader - custom user class
            return false;
        }
    }

    @SneakyThrows
    private Class<?> getClassSafe(String className) {
        return ClassUtils.getClass(className);
    }

    private PsiClass findLowestSuperClass(PsiClass class1, PsiClass class2) {
        if (class1 == null || class2 == null) {
            throw new RuntimeException("Can't find superclass when one of arguments is null");
        }

        while (!class2.isInheritorDeep(class1, null) && !class1.equals(class2)) {
            class1 = class1.getSuperClass();
        }

        return class1;
    }
}
