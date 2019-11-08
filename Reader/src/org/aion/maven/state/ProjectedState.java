package org.aion.maven.state;

import java.util.HashMap;
import java.util.Map;

import io.ipfs.multihash.Multihash;


/**
 * The projected state of the blockchain.
 * TODO:  Generalize this to handle reorganizations (doesn't impact this application but we should still handle it in the design).
 */
public class ProjectedState {
    private final Map<MavenTuple, Multihash> mapping;

    public ProjectedState() {
        this.mapping = new HashMap<>();
    }

    public synchronized void writeReference(MavenTuple tuple, Multihash reference) {
        this.mapping.put(tuple, reference);
    }

    public synchronized Multihash resolveReference(MavenTuple requestedTuple) {
        return this.mapping.get(requestedTuple);
    }
}
