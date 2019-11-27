package org.aion.maven.blockchain;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.maven.blockchain.events.PublishEvent;
import org.aion.maven.types.Address;
import org.aion.maven.types.BlockHash;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that we parse responses as expected.
 * This includes both the ResponseParser, directly, but also how individual event types are read from that parsed result (as testing
 * those in isolation isn't very useful).
 */
public class ResponseParserTest {
    @Test
    public void testParseEmpty() {
        String text = "{\"result\":[],\"id\":1,\"jsonrpc\":\"2.0\"}";
        List<TransactionLog> logs = ResponseParser.parseAsLogList(text);
        Assert.assertEquals(0, logs.size());
    }

    @Test
    public void testParseOne() {
        String text = "{\"result\":[{\"blockHash\":\"0x2b86707c029b067cd582a0060864ff8ef6ca0f3123473585166a0458b9d50190\",\"logIndex\":\"0x0\",\"address\":\"0xa056337bb14e818f3f53e13ab0d93b6539aa570cba91ce65c716058241989be9\",\"removed\":false,\"data\":\"0x0de0b6b3a7640000\",\"topics\":[\"0x426f6e6465640000000000000000000000000000000000000000000000000000\",\"0xa071d517ba6659067f5495149314e538463727099f3e5829b83b9b5e5eb398a4\"],\"blockNumber\":\"0x26e38\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x59124bb52af25ee0ee2ace15a7ddba8ebb7961a1a34787e499f0bb8f90326fa4\"}],\"id\":1,\"jsonrpc\":\"2.0\"}";
        List<TransactionLog> logs = ResponseParser.parseAsLogList(text);
        Assert.assertEquals(1, logs.size());
        Assert.assertEquals(2, logs.get(0).topics.size());
    }

    @Test
    public void testParseTwo() {
        String text = "{\"result\":[{\"blockHash\":\"0x2b86707c029b067cd582a0060864ff8ef6ca0f3123473585166a0458b9d50190\",\"logIndex\":\"0x0\",\"address\":\"0xa056337bb14e818f3f53e13ab0d93b6539aa570cba91ce65c716058241989be9\",\"removed\":false,\"data\":\"0x0de0b6b3a7640000\",\"topics\":[\"0x426f6e6465640000000000000000000000000000000000000000000000000000\",\"0xa071d517ba6659067f5495149314e538463727099f3e5829b83b9b5e5eb398a4\"],\"blockNumber\":\"0x26e38\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x59124bb52af25ee0ee2ace15a7ddba8ebb7961a1a34787e499f0bb8f90326fa4\"},{\"blockHash\":\"0x1ec291ced91cdebfee8379a1ab568ff3ef3f51e5f0253282e1d2334130f9f57f\",\"logIndex\":\"0x0\",\"address\":\"0xa056337bb14e818f3f53e13ab0d93b6539aa570cba91ce65c716058241989be9\",\"removed\":false,\"data\":\"0x0de0b6b3a7640000\",\"topics\":[\"0x426f6e6465640000000000000000000000000000000000000000000000000000\",\"0xa0e79df21eaec4e1691b45415ec4f536dd393669ad4074c3a8096147230cc634\"],\"blockNumber\":\"0x287bc\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0xe38c155fa6411aae2c01ef021b93a04dcc2591f93b00477ab26d14f2f14c40ea\"}],\"id\":1,\"jsonrpc\":\"2.0\"}";
        List<TransactionLog> logs = ResponseParser.parseAsLogList(text);
        Assert.assertEquals(2, logs.size());
    }

    @Test
    public void testParseInvalid() {
        String text = "{\"id\":1,\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32602,\"data\":\"Invalid block ids provided.\",\"message\":\"Invalid params\"}}";
        List<TransactionLog> logs = ResponseParser.parseAsLogList(text);
        Assert.assertNull(logs);
    }

    @Test
    public void testBigParse() {
        String text = ""
        // pre-amble
        + "{\"result\":["
        
        // StakerRegistryDeployed
        + "{\"blockHash\":\"0xe26e8b6b1585a1bd8003159b13ae091391afbcea50dec0591e9f2206aae1882a\",\"logIndex\":\"0x0\",\"address\":\"0xa056337bb14e818f3f53e13ab0d93b6539aa570cba91ce65c716058241989be9\",\"removed\":false,\"data\":\"0x3c\",\"topics\":[\"0x5374616b657252656769737472794465706c6f79656400000000000000000000\",\"0x00000000000000000000000000000000000000000000003635c9adc5dea00000\",\"0x000000000000000000000000000000000000000000000000000000000000ec40\",\"0x00000000000000000000000000000000000000000000000000000000000021c0\"],\"blockNumber\":\"0x1\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x107bda509b8b0bcca356cde33d810b8d64c0579e1c1937391f700d50ac1a2d2f\"},"
        
        // StakerRegistered
        + "{\"blockHash\":\"0x98fe097002d556dc6d457f9ce7a821a30c85928427c65f95af3241591623f66a\",\"logIndex\":\"0x0\",\"address\":\"0xa056337bb14e818f3f53e13ab0d93b6539aa570cba91ce65c716058241989be9\",\"removed\":false,\"data\":\"0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b\",\"topics\":[\"0x5374616b65725265676973746572656400000000000000000000000000000000\",\"0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b\",\"0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b\",\"0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b\"],\"blockNumber\":\"0x2\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x5de0407a87abcae0c7bcdde82f4d5f5d7a4684b31c5b64bd3c3854ebdd429cba\"},"
        
        // Maven on IPFS deployed
        + "{\"blockHash\":\"0x1f19495fb89a3ed4c49529589822985d878950fd61535d2d4640e157f1fb9587\",\"logIndex\":\"0x0\",\"address\":\"0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232\",\"removed\":false,\"data\":\"0x\",\"topics\":[\"0x4d6176656e206f6e2049504653206465706c6f79656400000000000000000000\"],\"blockNumber\":\"0x10\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x80f91a380a513bc5f05862f4f7c6d5a41a9ac16f21203abea15fdfbc343efefc\"},"
        
        // claimGroupId
        + "{\"blockHash\":\"0x158f079fe6e037b104e093ebdd13a091e75b1aa5bb5f7e6a9082e5ee58c2a266\",\"logIndex\":\"0x0\",\"address\":\"0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232\",\"removed\":false,\"data\":\"0x\",\"topics\":[\"0x636c61696d47726f757049640000000000000000000000000000000000000000\",\"0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b\",\"0x6f72672e61696f6e2e6d6176656e000000000000000000000000000000000000\"],\"blockNumber\":\"0x15\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0xeb9367e991f5bf4511bd02a0ce4f64167c637546223b7712d9f2e63e546cbf30\"},"
        
        // publish - note that this data is just duplicated from the one below (we can rely on the data being correct, in actual deployment)
        + "{\"blockHash\":\"0xa11e2fa0080d62d0faa9609c67f2825ad5005cc8fe9604443dcff8148a51a587\",\"logIndex\":\"0x0\",\"address\":\"0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232\",\"removed\":false,\"data\":\"0x210003312e30010021002e516d6445437052474c4153617076386a664e6450526437475550674768324d774268454d436b4563684d704d5a62\",\"topics\":[\"0x7075626c69736800000000000000000000000000000000000000000000000000\",\"0x6f72672e61696f6e2e6d6176656e000000000000000000000000000000000000\",\"0x3132333400000000000000000000000000000000000000000000000000000000\"],\"blockNumber\":\"0x17\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x81d04728eb32b623c988012f09e11b29897192c17918c176857eacfa04f43e49\"},"
        
        // publish
        + "{\"blockHash\":\"0xbf32e73f71944afa9955bfd69e2ee2f26c4d1ea4f3eb774d212619de0e9d878f\",\"logIndex\":\"0x0\",\"address\":\"0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232\",\"removed\":false,\"data\":\"0x210003312e30010021002e516d6445437052474c4153617076386a664e6450526437475550674768324d774268454d436b4563684d704d5a62\",\"topics\":[\"0x7075626c69736800000000000000000000000000000000000000000000000000\",\"0x6f72672e61696f6e2e6d6176656e000000000000000000000000000000000000\",\"0x3132333400000000000000000000000000000000000000000000000000000000\"],\"blockNumber\":\"0x1e\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x97e4c96ef53cae44c28b8a5f0f8aa7b740137c89a87c37a7127d728ce8f37ca3\"},"
        
        // deClaimGroupId
        + "{\"blockHash\":\"0xd6f8b0a3e65d19ee4f8ae48dcb03118b1dcaa01a45abf33de7cb7299d114e405\",\"logIndex\":\"0x0\",\"address\":\"0xa01f53d4e4521941c68ba3570f0a4f1618363ceed09b45fa715b230eaae1a232\",\"removed\":false,\"data\":\"0x\",\"topics\":[\"0x6465436c61696d47726f75704964000000000000000000000000000000000000\",\"0xa02df9004be3c4a20aeb50c459212412b1d0a58da3e1ac70ba74dde6b4accf4b\",\"0x6f72672e61696f6e2e6d6176656e000000000000000000000000000000000000\"],\"blockNumber\":\"0x1f\",\"transactionIndex\":\"0x0\",\"transactionHash\":\"0x952f7783288b520dbbe18506ff32b844791a4195ba0d67967401a2207f2d4b67\"}"
        
        //post-amble
        + "],\"id\":1,\"jsonrpc\":\"2.0\"}";
        
        List<TransactionLog> logs = ResponseParser.parseAsLogList(text);
        Assert.assertEquals(7, logs.size());
        Assert.assertEquals("StakerRegistryDeployed", logs.get(0).topics.get(0).extractAsNullTerminatedString());
        Assert.assertFalse(verifyPublish(logs.get(0)));
        Assert.assertEquals("StakerRegistered", logs.get(1).topics.get(0).extractAsNullTerminatedString());
        Assert.assertFalse(verifyPublish(logs.get(1)));
        Assert.assertEquals("Maven on IPFS deployed", logs.get(2).topics.get(0).extractAsNullTerminatedString());
        Assert.assertFalse(verifyPublish(logs.get(2)));
        Assert.assertEquals("claimGroupId", logs.get(3).topics.get(0).extractAsNullTerminatedString());
        Assert.assertFalse(verifyPublish(logs.get(3)));
        Assert.assertEquals("publish", logs.get(4).topics.get(0).extractAsNullTerminatedString());
        Assert.assertTrue(verifyPublish(logs.get(4)));
        Assert.assertEquals("publish", logs.get(5).topics.get(0).extractAsNullTerminatedString());
        Assert.assertTrue(verifyPublish(logs.get(5)));
        Assert.assertEquals("deClaimGroupId", logs.get(6).topics.get(0).extractAsNullTerminatedString());
        Assert.assertFalse(verifyPublish(logs.get(6)));
    }
    @Test
    public void testDirectPublishEvent() {
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

    private static boolean verifyPublish(TransactionLog log) {
        PublishEvent event = null;
        try {
            event = PublishEvent.readFromLog(log);
        } catch (IllegalArgumentException e) {
            // This is the way the interpretation fails.
            event = null;
        }
        return (null != event);
    }
}
