package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.reflections.Reflections;

import java.util.Set;

class ReflectionService {

    private static final Reflections REFLECTIONS = new Reflections();

    Set<Class<?>> getSubclassesOf(Class<?> superClass) {
        return Sets.union(
                REFLECTIONS.getSubTypesOf(superClass),
                ImmutableSet.of(superClass));
    }
}