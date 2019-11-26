package org.aion.maven.state;

import java.util.HashMap;
import java.util.Map;


/**
 * The projected state of the blockchain.
 * TODO:  Generalize this to expose reorganizations/finality (doesn't impact this application but we should still handle it in the design).
 */
public class ProjectedState<T> {
    private final Map<MavenTuple, T> mapping;

    public ProjectedState() {
        this.mapping = new HashMap<>();
    }

    public synchronized void writeReference(MavenTuple tuple, T reference) {
        this.mapping.put(tuple, reference);
    }

    public synchronized void clearReference(MavenTuple tuple) {
        this.mapping.remove(tuple);
    }

    public synchronized T resolveReference(MavenTuple requestedTuple) {
        return this.mapping.get(requestedTuple);
    }
}
