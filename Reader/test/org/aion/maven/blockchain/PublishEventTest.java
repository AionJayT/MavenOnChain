package org.aion.maven.blockchain;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.maven.types.Address;
import org.aion.maven.types.BlockHash;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that PublishEvent creation works.
 */
public class PublishEventTest {
    @Test
    public void testBasic() {
        Address sourceContractAddress = new Address(build32Bytes(1));
        String base58Hash = "QmWmyoMoctfbAaiEs2G46gpeUmhqFRDW6KWo64y5r581Vz";
        String version = "1.0";
        byte[] data = new ABIStreamingEncoder()
                .encodeOneString(version)
                .encodeOneByte((byte)0)
                .encodeOneString(base58Hash)
                .toBytes();
        String groupId = "groupId";
        String artifactId = "artifactId";
        List<Topic> topics = List.of(createStringTopic("publish")
                , createStringTopic(groupId)
                , createStringTopic(artifactId)
        );
        TransactionLog log = new TransactionLog(sourceContractAddress, data, topics, 1, new BlockHash(build32Bytes(1)), 0, 1);
        PublishEvent event = PublishEvent.readFromLog(log);
        Assert.assertEquals(groupId, event.mavenTuple.groupId);
        Assert.assertEquals(artifactId, event.mavenTuple.artifactId);
        Assert.assertEquals(version, event.mavenTuple.version);
        Assert.assertEquals("pom", event.mavenTuple.type);
        Assert.assertEquals(base58Hash, event.ipfsMultihash.toBase58());
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

    private static Topic createStringTopic(String string) {
        return Topic.createFromArbitraryData(string.getBytes(StandardCharsets.UTF_8));
    }
}
