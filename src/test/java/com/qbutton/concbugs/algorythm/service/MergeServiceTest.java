package com.qbutton.concbugs.algorythm.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.EnvEntry;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import com.qbutton.concbugs.algorythm.exception.AlgorithmValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@DisplayName("MergeService class")
@ExtendWith(MockitoExtension.class)
class MergeServiceTest {

    private MergeService mergeService;

    @Mock
    private ClassFinderService classFinderService;
    @Mock
    private GraphService graphService;

    @BeforeEach
    void init() {
        mergeService = new MergeService(classFinderService, graphService);
    }

    @Test
    @DisplayName("merges graphs correctly")
    void mergeGraphs() {
        //given

        /* graph 1
            ho1 -> ho2
            ho1 -> ho3
            ho3 -> ho2
            ho2 -> 0
        */
        ProgramPoint point1 = new ProgramPoint("a", 1);
        HeapObject ho1 = new HeapObject(point1, "int");

        ProgramPoint point2 = new ProgramPoint("b", 2);
        HeapObject ho2 = new HeapObject(point2, "java.lang.String");

        ProgramPoint point3 = new ProgramPoint("c", 3);
        HeapObject ho3 = new HeapObject(point3, "java.lang.Object");

        Graph g1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2, ho3),
                ho3, ImmutableSet.of(ho2),
                ho2, Collections.emptySet()
        ));

        /* graph2
            ho4 -> ho2
            ho1 -> ho4
            ho2 -> ho4
        */
        ProgramPoint point4 = new ProgramPoint("d", 4);
        HeapObject ho4 = new HeapObject(point4, "java.util.Map");

        Graph g2 = new Graph(ImmutableMap.of(
                ho4, ImmutableSet.of(ho2),
                ho1, ImmutableSet.of(ho4),
                ho2, ImmutableSet.of(ho4)
        ));

        //when
        Graph merged = mergeService.mergeGraphs(g1, g2);

        //then
        /* should be
            ho1 -> ho2
            ho1 -> ho3
            ho1 -> ho4
            ho3 -> ho2
            ho4 -> ho2
            ho2 -> ho4
         */

        Map<HeapObject, Set<HeapObject>> graphMap = merged.getNeighbors();
        assertThat(graphMap.size(), is(4));
        assertTrue(graphMap.containsKey(ho1));
        assertTrue(graphMap.containsKey(ho2));
        assertTrue(graphMap.containsKey(ho3));
        assertTrue(graphMap.containsKey(ho4));
        assertThat(graphMap.get(ho1).size(), is(3));
        assertTrue(graphMap.get(ho1).contains(ho2));
        assertTrue(graphMap.get(ho1).contains(ho3));
        assertTrue(graphMap.get(ho1).contains(ho4));
        assertThat(graphMap.get(ho2).size(), is(1));
        assertTrue(graphMap.get(ho2).contains(ho4));
        assertThat(graphMap.get(ho3).size(), is(1));
        assertTrue(graphMap.get(ho3).contains(ho2));
        assertThat(graphMap.get(ho4).size(), is(1));
        assertTrue(graphMap.get(ho4).contains(ho2));
    }

    @Nested
    @DisplayName("merges envs")
    class MergeEnvs {

        @Test
        @DisplayName("correctly when validation is ok")
        void mergeEnvs() {
            //given
            String varName1 = "v1";
            HeapObject ho1 = new HeapObject(new ProgramPoint(varName1, 10), "int");
            String varName2 = "v2";
            HeapObject ho2 = new HeapObject(new ProgramPoint(varName2, 11), "java.lang.String");
            HeapObject ho3 = new HeapObject(new ProgramPoint(varName1, 12), "java.lang.Number");

            List<EnvEntry> env1 = ImmutableList.of(
                    new EnvEntry(varName1, ho1),
                    new EnvEntry(varName2, ho2)
            );
            List<EnvEntry> env2 = ImmutableList.of(
                    new EnvEntry(varName1, ho3),
                    new EnvEntry(varName2, ho2)
            );
            when(classFinderService.findLowestSuperClass("int", "java.lang.Number"))
                    .thenReturn("java.lang.Number");
            doCallRealMethod().when(graphService).addOrReplaceEnv(any(), any());

            //when
            List<EnvEntry> mergedEnv = mergeService.mergeEnvs(env1, env2, 14);

            //then
            assertThat(mergedEnv.size(), is(2));
            assertThat(mergedEnv.get(1).getVarName(), is(varName2));
            assertThat(mergedEnv.get(1).getHeapObject(), is(ho2));

            EnvEntry mergedEnvEntry = mergedEnv.get(0);
            HeapObject newHeapObject = mergedEnvEntry.getHeapObject();
            assertThat(mergedEnvEntry.getVarName(), is(varName1));
            assertSame(newHeapObject.getClazz(), "java.lang.Number");
            assertThat(newHeapObject.getProgramPoint().getLineNumber(), is(14));
            assertThat(newHeapObject.getProgramPoint().getVariableName(), is(varName1));
        }

        @Test
        @DisplayName("with exception when validation is not ok")
        void mergeEnvs_envsAreDifferent() {
            //given
            String varName1 = "v1";
            HeapObject ho1 = new HeapObject(new ProgramPoint(varName1, 10), "int");
            String varName2 = "v2";
            String varName3 = "v3";
            HeapObject ho2 = new HeapObject(new ProgramPoint(varName2, 11), "java.lang.String");

            List<EnvEntry> env1 = ImmutableList.of(
                    new EnvEntry(varName1, ho1),
                    new EnvEntry(varName2, ho2)
            );
            List<EnvEntry> env2 = ImmutableList.of(
                    new EnvEntry(varName1, ho1),
                    new EnvEntry(varName3, ho2)
            );

            //when
            //then
            assertThrows(AlgorithmValidationException.class,
                    () -> mergeService.mergeEnvs(env1, env2, 30));
        }
    }
}