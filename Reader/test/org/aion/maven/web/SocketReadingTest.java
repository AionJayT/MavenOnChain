package org.aion.maven.web;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.aion.maven.state.MavenTuple;
import org.junit.Assert;
import org.junit.Test;


public class SocketReadingTest {
    @Test
    public void testBasicParse() {
        MavenTuple tuple = SocketReading.attemptTupleParse("GET /aion/art/3.8.1/art-3.8.1.jar HTTP/1.1");
        Assert.assertNotNull(tuple);
        Assert.assertEquals("aion", tuple.groupId);
        Assert.assertEquals("art", tuple.artifactId);
        Assert.assertEquals("3.8.1", tuple.version);
        Assert.assertEquals("jar", tuple.type);
    }

    @Test
    public void testStreamParse() throws Exception {
        String content = "GET /aion/art/3.8.1/art-3.8.1.jar HTTP/1.1\r\n"
                + "\r\n";
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        MavenTuple tuple = SocketReading.readRequest(stream);
        Assert.assertNotNull(tuple);
        Assert.assertEquals("aion", tuple.groupId);
        Assert.assertEquals("art", tuple.artifactId);
        Assert.assertEquals("3.8.1", tuple.version);
        Assert.assertEquals("jar", tuple.type);
    }

    @Test
    public void testMultiStreamParse() throws Exception {
        String content = "GET /aion/art/3.8.1/art-3.8.1.jar HTTP/1.1\r\n"
                + "User-Agent: Apache-Maven/3.3.9 (Java 11.0.1; Linux 4.15.0-58-generic)\r\n"
                + "\r\n";
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        MavenTuple tuple = SocketReading.readRequest(stream);
        Assert.assertNotNull(tuple);
        Assert.assertEquals("aion", tuple.groupId);
        Assert.assertEquals("art", tuple.artifactId);
        Assert.assertEquals("3.8.1", tuple.version);
        Assert.assertEquals("jar", tuple.type);
    }
}
