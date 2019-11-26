package org.aion.maven.types;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that the basics of the types work correctly.
 */
public class TypesTest {
    @Test
    public void testAddress() {
        Address one1 = new Address(build32Bytes(1));
        Address one2 = new Address(build32Bytes(1));
        Address two1 = new Address(build32Bytes(2));
        
        Assert.assertEquals(one1.hashCode(), one2.hashCode());
        Assert.assertEquals(one2.hashCode(), one1.hashCode());
        Assert.assertNotEquals(one1.hashCode(), two1.hashCode());
        
        Assert.assertEquals(one1, one2);
        Assert.assertEquals(one2, one1);
        Assert.assertNotEquals(one1, two1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddress_invalidSize() {
        Address one1 = new Address(new byte[31]);
        Assert.assertNotNull(one1);
    }

    @Test
    public void testBlockHash() {
        BlockHash one1 = new BlockHash(build32Bytes(1));
        BlockHash one2 = new BlockHash(build32Bytes(1));
        BlockHash two1 = new BlockHash(build32Bytes(2));
        
        Assert.assertEquals(one1.hashCode(), one2.hashCode());
        Assert.assertEquals(one2.hashCode(), one1.hashCode());
        Assert.assertNotEquals(one1.hashCode(), two1.hashCode());
        
        Assert.assertEquals(one1, one2);
        Assert.assertEquals(one2, one1);
        Assert.assertNotEquals(one1, two1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlockHash_invalidSize() {
        BlockHash one1 = new BlockHash(new byte[31]);
        Assert.assertNotNull(one1);
    }

    @Test
    public void testTopic() {
        Topic one1 = Topic.createFromPreciseData(build32Bytes(1));
        Topic one2 = Topic.createFromPreciseData(build32Bytes(1));
        Topic two1 = Topic.createFromPreciseData(build32Bytes(2));
        
        Assert.assertEquals(one1.hashCode(), one2.hashCode());
        Assert.assertEquals(one2.hashCode(), one1.hashCode());
        Assert.assertNotEquals(one1.hashCode(), two1.hashCode());
        
        Assert.assertEquals(one1, one2);
        Assert.assertEquals(one2, one1);
        Assert.assertNotEquals(one1, two1);
    }

    @Test
    public void testTopicMissized() {
        byte[] thirtyOne = new byte[31];
        fill(thirtyOne, 1);
        byte[] thirtyThree = new byte[33];
        fill(thirtyThree, 1);
        // We want this to end in zeros since we are going to compare against zero-padding.
        thirtyThree[31] = 0;
        thirtyThree[32] = 0;
        Topic one1 = Topic.createFromArbitraryData(thirtyOne);
        Topic one2 = Topic.createFromArbitraryData(thirtyThree);
        Topic two1 = Topic.createFromPreciseData(build32Bytes(2));
        
        Assert.assertEquals(one1.hashCode(), one2.hashCode());
        Assert.assertEquals(one2.hashCode(), one1.hashCode());
        Assert.assertNotEquals(one1.hashCode(), two1.hashCode());
        
        Assert.assertEquals(one1, one2);
        Assert.assertEquals(one2, one1);
        Assert.assertNotEquals(one1, two1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTopicInvalidConstructor() {
        byte[] thirtyOne = new byte[31];
        fill(thirtyOne, 1);
        Topic one1 = Topic.createFromPreciseData(thirtyOne);
        Assert.assertNotNull(one1);
    }

    @Test
    public void testTransactionLog() {
        // Observe that we only care about the blockhash, transaction index, and log index (last 3 args).
        TransactionLog one1 = new TransactionLog(new Address(build32Bytes(1)), new byte[] {1}, Collections.emptyList(), 1, new BlockHash(build32Bytes(1)), 0, 0);
        TransactionLog one2 = new TransactionLog(new Address(build32Bytes(2)), new byte[] {1}, Collections.emptyList(), 1, new BlockHash(build32Bytes(1)), 0, 0);
        TransactionLog two1 = new TransactionLog(new Address(build32Bytes(1)), new byte[] {1}, Collections.emptyList(), 1, new BlockHash(build32Bytes(1)), 0, 1);
        
        Assert.assertEquals(one1.hashCode(), one2.hashCode());
        Assert.assertEquals(one2.hashCode(), one1.hashCode());
        Assert.assertNotEquals(one1.hashCode(), two1.hashCode());
        
        Assert.assertEquals(one1, one2);
        Assert.assertEquals(one2, one1);
        Assert.assertNotEquals(one1, two1);
    }

    @Test
    public void testTopicAsString() {
        String foobar = "foobar";
        Topic topic = Topic.createFromArbitraryData(foobar.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(foobar, topic.extractAsNullTerminatedString());
    }


    private static byte[] build32Bytes(int element) {
        byte[] array = new byte[32];
        fill(array, element);
        return array;
    }

    private static void fill(byte[] array, int element) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = (byte)element;
        }
    }
}
