package org.aion.maven.state;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the basic ProjectedState mechanics.
 */
public class ProjectedStateTest {
    @Test
    public void testCommon() {
        ProjectedState<String> state = new ProjectedState<>();
        state.writeReference(makeTuple("1.0"), "value");
        Assert.assertEquals("value", state.resolveReference(makeTuple("1.0")));
        Assert.assertNull(state.resolveReference(makeTuple("2.0")));
        state.clearReference(makeTuple("1.0"));
        Assert.assertNull(state.resolveReference(makeTuple("1.0")));
    }


    private static MavenTuple makeTuple(String version) {
        return new MavenTuple("group", "artifact", version, "pom");
    }
}
