package com.qbutton.concbugs.algorythm.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.qbutton.concbugs.algorythm.dto.Graph;
import com.qbutton.concbugs.algorythm.dto.HeapObject;
import com.qbutton.concbugs.algorythm.dto.ProgramPoint;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MergeUtilsTest {

    @Test
    public void removeObject() {
        //given

        /*
            ho1 -> ho2
            ho1 -> ho3
            ho3 -> ho2
            ho3 -> ho1
        */
        ProgramPoint point1 = new ProgramPoint("a", 1);
        HeapObject ho1 = new HeapObject(point1, Integer.class);

        ProgramPoint point2 = new ProgramPoint("b", 2);
        HeapObject ho2 = new HeapObject(point2, String.class);

        ProgramPoint point3 = new ProgramPoint("c", 3);
        HeapObject ho3 = new HeapObject(point3, Object.class);

        Map<HeapObject, Set<HeapObject>> graphMap = ImmutableMap.of(
                ho1, ImmutableSet.of(ho2, ho3),
                ho3, ImmutableSet.of(ho2, ho1)
        );

        Graph initialGraph = new Graph(graphMap);

        //when
        Graph graph = MergeUtils.removeObject(initialGraph, ho1);

        //then
        //ho3 -> ho2 is the only remaining edge
        Map<HeapObject, Set<HeapObject>> neighbors = graph.getNeighbors();
        assertThat(neighbors.size(), is(1));
        assertTrue(neighbors.containsKey(ho3));
        assertThat(neighbors.get(ho3).size(), is(1));
        assertThat(neighbors.get(ho3).iterator().next(), is(ho2));
    }

    @Test
    public void mergeGraphs() {
        //given

        /* graph 1
            ho1 -> ho2
            ho1 -> ho3
            ho3 -> ho2
        */
        ProgramPoint point1 = new ProgramPoint("a", 1);
        HeapObject ho1 = new HeapObject(point1, Integer.class);

        ProgramPoint point2 = new ProgramPoint("b", 2);
        HeapObject ho2 = new HeapObject(point2, String.class);

        ProgramPoint point3 = new ProgramPoint("c", 3);
        HeapObject ho3 = new HeapObject(point3, Object.class);

        Graph g1 = new Graph(ImmutableMap.of(
                ho1, ImmutableSet.of(ho2, ho3),
                ho3, ImmutableSet.of(ho2)
        ));

        /* graph2
            ho4 -> ho2
            ho1 -> ho4
        */
        ProgramPoint point4 = new ProgramPoint("d", 4);
        HeapObject ho4 = new HeapObject(point4, Map.class);

        Graph g2 = new Graph(ImmutableMap.of(
                ho4, ImmutableSet.of(ho2),
                ho1, ImmutableSet.of(ho4)
        ));

        //when
        Graph merged = MergeUtils.mergeGraphs(g1, g2);

        //then
        /* should be
            ho1 -> ho2
            ho1 -> ho3
            ho1 -> ho4
            ho3 -> ho2
            ho4 -> ho2
         */

        Map<HeapObject, Set<HeapObject>> graphMap = merged.getNeighbors();
        assertThat(graphMap.size(), is(3));
        assertTrue(graphMap.containsKey(ho1));
        assertTrue(graphMap.containsKey(ho3));
        assertTrue(graphMap.containsKey(ho4));
        assertThat(graphMap.get(ho1).size(), is(3));
        assertTrue(graphMap.get(ho1).contains(ho2));
        assertTrue(graphMap.get(ho1).contains(ho3));
        assertTrue(graphMap.get(ho1).contains(ho4));
        assertThat(graphMap.get(ho3).size(), is(1));
        assertTrue(graphMap.get(ho3).contains(ho2));
        assertThat(graphMap.get(ho4).size(), is(1));
        assertTrue(graphMap.get(ho4).contains(ho2));
    }
}