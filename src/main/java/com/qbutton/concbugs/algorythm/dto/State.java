package com.qbutton.concbugs.algorythm.dto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * The state is a 5-tuple consisting of:
 *
 * - The current lock-order graph. Each node in the graph is a symbolic heap
 * object. The graph represents possible locking behavior for concrete heap
 * objects drawn from the sets modeled by the symbolic heap objects. A path
 * of nodes o1 ... ok in the graph corresponds to a potential program path in
 * which o1 is locked, then o2 is locked (before o1 is released), and so on.
 *
 * - The roots of the graph. The roots represent objects that are locked at some
 * point during execution of a given method when no other lock is held.
 *
 * - The list of locks that are currently held, in the order in which they were
 * obtained.
 *
 * - An environment mapping local variables to symbolic heap objects. The
 * environment is an important component of the interprocedural analysis, as it
 * allows information to propagate between callers and callees. It also improves
 * precision by tracking the
 * ow of values between local variables.
 *
 * - A set of objects that have had wait called on them without an enclosing
 * synchronized statement in the current method.
 */
@Data
public class State implements Cloneable {
    private final Graph graph;
    private final Set<HeapObject> roots;
    private final List<HeapObject> locks;
    /**
     * Environment should have 'this' if it is instance method put at the beginning of the method, or 'Class.class' if it is
     * static method.
     */
    private final List<EnvEntry> environment;
    private final Set<HeapObject> waits;

    @SuppressFBWarnings("CN_IDIOM_NO_SUPER_CALL")
    @Override
    public State clone() {
        return new State(
                graph.clone(),
                ImmutableSet.copyOf(roots),
                ImmutableList.copyOf(locks),
                ImmutableList.copyOf(environment),
                ImmutableSet.copyOf(waits)
        );
    }
}
