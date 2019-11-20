package org.aion.maven.blockchain;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aion.maven.types.BlockHash;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that the core state machine works as expected.
 * Note that we expect that the address filtering is done at a higher level, so this doesn't care.
 */
public class LogReaderStateMachineTest {
    @Test
    public void testNormalIngest() {
        // Just do a deployment and a single event after that.
        TestListener listener = new TestListener();
        LogReaderStateMachine machine = new LogReaderStateMachine(listener, 1);
        Assert.assertEquals(1L, machine.getPollingBlockNumber());
        Topic testTopic = Topic.createFromArbitraryData(new byte[] {(byte)1});
        machine.consumeTransactionLogs(Collections.singletonList(createLog(testTopic, 1, 1, 1, 0)));
        Assert.assertEquals(1L, machine.getPollingBlockNumber());
        Assert.assertEquals(1, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        machine.consumeTransactionLogs(List.of(createLog(testTopic, 1, 1, 1, 0),
                createLog(testTopic, 2, 2, 2, 0)));
        Assert.assertEquals(2L, machine.getPollingBlockNumber());
        Assert.assertEquals(2, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        Assert.assertEquals(2, listener.consumes);
        Assert.assertEquals(0, listener.reverts);
    }

    @Test
    public void testDeployAndIngestAtOnce() {
        // Just do a deployment and a single event in the same poll.
        TestListener listener = new TestListener();
        LogReaderStateMachine machine = new LogReaderStateMachine(listener, 1);
        Topic testTopic = Topic.createFromArbitraryData(new byte[] {(byte)1});
        machine.consumeTransactionLogs(List.of(createLog(testTopic, 1, 1, 1, 0)
                , createLog(testTopic, 2, 2, 2, 0)
        ));
        Assert.assertEquals(2L, machine.getPollingBlockNumber());
        Assert.assertEquals(2, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        Assert.assertEquals(2, listener.consumes);
        Assert.assertEquals(0, listener.reverts);
    }

    @Test
    public void testDenseIngest() {
        // Deploy and then send a bunch of logs from the same block.
        TestListener listener = new TestListener();
        LogReaderStateMachine machine = new LogReaderStateMachine(listener, 1);
        Topic testTopic = Topic.createFromArbitraryData(new byte[] {(byte)1});
        machine.consumeTransactionLogs(Collections.singletonList(createLog(testTopic, 1, 1, 1, 0)));
        Assert.assertEquals(1, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        machine.consumeTransactionLogs(List.of(createLog(testTopic, 1, 1, 1, 0)
                , createLog(testTopic, 4, 2, 2, 0)
                , createLog(testTopic, 2, 2, 2, 1)
                , createLog(testTopic, 3, 2, 2, 2)
        ));
        Assert.assertEquals(2L, machine.getPollingBlockNumber());
        Assert.assertEquals(3, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        Assert.assertEquals(4, listener.consumes);
        Assert.assertEquals(0, listener.reverts);
    }

    @Test
    public void testRevertAndDeployOnly() {
        // Deploy, then revert with a conflicting deployment.
        TestListener listener = new TestListener();
        LogReaderStateMachine machine = new LogReaderStateMachine(listener, 1);
        Topic testTopic = Topic.createFromArbitraryData(new byte[] {(byte)1});
        machine.consumeTransactionLogs(Collections.singletonList(createLog(testTopic, 1, 1, 1, 0)));
        Assert.assertEquals(1L, machine.getPollingBlockNumber());
        Assert.assertEquals(1, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        machine.consumeTransactionLogs(Collections.singletonList(createLog(testTopic, 2, 1, 2, 0)));
        Assert.assertEquals(1L, machine.getPollingBlockNumber());
        // (this should be the state where we merely reverted, so there should be nothing)
        Assert.assertNull(listener.fakeTable.get(testTopic.renderedAsHexNumber()));
        Assert.assertEquals(1, listener.consumes);
        Assert.assertEquals(1, listener.reverts);
        
        machine.consumeTransactionLogs(Collections.singletonList(createLog(testTopic, 2, 1, 2, 0)));
        Assert.assertEquals(1L, machine.getPollingBlockNumber());
        Assert.assertEquals(2, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        Assert.assertEquals(2, listener.consumes);
        Assert.assertEquals(1, listener.reverts);
    }

    @Test
    public void testIngestRevertAndIngest() {
        // Deploy, ingest some data, then partially revert, then ingest more data.
        TestListener listener = new TestListener();
        LogReaderStateMachine machine = new LogReaderStateMachine(listener, 1);
        Topic testTopic = Topic.createFromArbitraryData(new byte[] {(byte)1});
        machine.consumeTransactionLogs(Collections.singletonList(createLog(testTopic, 1, 1, 1, 0)));
        Assert.assertEquals(1, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        machine.consumeTransactionLogs(List.of(createLog(testTopic, 1, 1, 1, 0)
                , createLog(testTopic, 4, 2, 2, 0)
                , createLog(testTopic, 2, 2, 2, 1)
                , createLog(testTopic, 3, 3, 3, 0)
        ));
        // Check the current state before we do the revert.
        Assert.assertEquals(3L, machine.getPollingBlockNumber());
        Assert.assertEquals(3, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        Assert.assertEquals(4, listener.consumes);
        Assert.assertEquals(0, listener.reverts);
        
        // Do the revert, as we would normally see it.
        machine.consumeTransactionLogs(List.of(createLog(testTopic, 3, 3, 4, 0)
        ));
        Assert.assertEquals(2L, machine.getPollingBlockNumber());
        Assert.assertNull(listener.fakeTable.get(testTopic.renderedAsHexNumber()));
        Assert.assertEquals(4, listener.consumes);
        Assert.assertEquals(1, listener.reverts);
        
        // Continue, after the expected revert behaviour.
        machine.consumeTransactionLogs(List.of(createLog(testTopic, 4, 2, 2, 0)
                , createLog(testTopic, 2, 2, 2, 1)
                , createLog(testTopic, 3, 3, 4, 0)
        ));
        Assert.assertEquals(3L, machine.getPollingBlockNumber());
        Assert.assertEquals(3, listener.fakeTable.get(testTopic.renderedAsHexNumber())[0]);
        Assert.assertEquals(5, listener.consumes);
        Assert.assertEquals(1, listener.reverts);
    }


    private static TransactionLog createLog(Topic testTopic, int dataByte, long blockNumber, int hashBase, int transactionIndex) {
        return new TransactionLog(null, new byte[] { (byte)dataByte }, Collections.singletonList(testTopic), blockNumber, new BlockHash(build32Bytes(hashBase)), transactionIndex, 0);
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


    private static class TestListener implements ILogStateListener {
        // We just key this by the rendered topic and assign the data.
        // (this representation does NOT store enough history to correctly handle reverts but that isn't the point).
        public final Map<String,byte[]> fakeTable = new HashMap<>();
        public int consumes = 0;
        public int reverts = 0;

        @Override
        public void consumedLog(TransactionLog log) {
            this.fakeTable.put(log.topics.get(0).renderedAsHexNumber(), log.data);
            this.consumes += 1;
        }

        @Override
        public void revertedLog(TransactionLog log) {
            this.fakeTable.remove(log.topics.get(0).renderedAsHexNumber());
            this.reverts += 1;
        }
        
    }
}
