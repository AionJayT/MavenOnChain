package org.aion.maven.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * The projected state of the blockchain.
 * TODO:  Generalize this to expose reorganizations/finality (doesn't impact this application but we should still handle it in the design).
 */
public class ProjectedState<T> {
    private final Map<MavenTuple, T> mapping;
    private final Set<String> claimedGroupIds;

    public ProjectedState() {
        this.mapping = new HashMap<>();
        this.claimedGroupIds = new HashSet<>();
    }

    public synchronized void writeReference(MavenTuple tuple, T reference) {
        if (!this.claimedGroupIds.contains(tuple.groupId)) {
            throw new IllegalStateException("Group not claimed for publish: " + tuple.groupId);
        }
        if (this.mapping.containsKey(tuple)) {
            throw new IllegalStateException("Tuple already written: " + tuple);
        }
        this.mapping.put(tuple, reference);
    }

    public synchronized void clearReference(MavenTuple tuple) {
        if (!this.claimedGroupIds.contains(tuple.groupId)) {
            throw new IllegalStateException("Group not claimed for clear: " + tuple.groupId);
        }
        if (!this.mapping.containsKey(tuple)) {
            throw new IllegalStateException("Tuple NOT in mapping: " + tuple);
        }
        this.mapping.remove(tuple);
    }

    public synchronized T resolveReference(MavenTuple requestedTuple) {
        return this.mapping.get(requestedTuple);
    }

    public synchronized void addClaim(String groupId) {
        if (this.claimedGroupIds.contains(groupId)) {
            throw new IllegalStateException("Group already claimed: " + groupId);
        }
        this.claimedGroupIds.add(groupId);
    }

    public synchronized void removeClaim(String groupId) {
        if (!this.claimedGroupIds.contains(groupId)) {
            throw new IllegalStateException("Group not claimed: " + groupId);
        }
        this.claimedGroupIds.remove(groupId);
    }
}
