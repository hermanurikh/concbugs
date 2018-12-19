package com.qbutton.concbugs.algorythm.service;

import org.reflections.Reflections;

import java.util.Collections;
import java.util.Set;

class ReflectionService {

    private static final Reflections REFLECTIONS = new Reflections();

    Set<String> getSubclassesOf(String superClass) {
        //todo !
        return Collections.emptySet();
        /*return Sets.union(
                REFLECTIONS.getSubTypesOf(superClass),
                ImmutableSet.of(superClass));*/
    }
}