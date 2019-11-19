package org.aion.maven.blockchain;

import java.util.List;

import org.aion.maven.types.TransactionLog;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that we parse responses as expected.
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
}
