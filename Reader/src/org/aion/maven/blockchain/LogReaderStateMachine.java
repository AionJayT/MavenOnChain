package org.aion.maven.blockchain;

import java.util.List;
import java.util.Stack;

import org.aion.maven.types.BlockHash;
import org.aion.maven.types.TransactionLog;


/**
 * The class responsible for the core logic of how to manage the event log stream:
 * -advancing when it is in a good state
 * -reversing when it is reorganizing
 */
public final class LogReaderStateMachine {
    private final ILogStateListener listener;
    private final long startingBlockNumber;
    private Stack<TransactionLog> observedLogs;
    private long nextPollingBlockNumber;
    // Note that expected hash is null only when the deployment hasn't yet been observed - otherwise, we always saw at least one event so we know what to expect.
    private BlockHash expectedHash;

    /**
     * Creates a new LogReaderStateMachine which has consumed no logs.
     * 
     * @param listener The ILogStateListener which will be notified when the receiver consumes or reverts block logs.
     * @param startingBlockNumber The block number the instance should consider "the beginning of time".
     */
    public LogReaderStateMachine(ILogStateListener listener, long startingBlockNumber) {
        this.listener = listener;
        this.startingBlockNumber = startingBlockNumber;
        this.observedLogs = new Stack<>();
        this.nextPollingBlockNumber = startingBlockNumber;
    }

    /**
     * @return The block number where the next eth_getLogs poll should start.
     */
    public long getPollingBlockNumber() {
        return this.nextPollingBlockNumber;
    }

    /**
     * Consumes the given logs, deciding if the state should advance or revert, and notifying the receiver's ILogStateListener
     * for each such decision, on a per-log basis.
     * 
     * @param logs The sorted logs starting with those in block getPollingBlockNumber().
     */
    public void consumeTransactionLogs(List<TransactionLog> logs) {
        if (logs.isEmpty()) {
            throw new IllegalArgumentException("Logs being empty should only happen if the node is resyncing or HEAVILY reorganized so we will just wait to see this block height - don't call the state machine with this!");
        }
        if (null == expectedHash) {
            // We haven't even seen the deployment so just consume whatever this is.
            for (TransactionLog log : logs) {
                absorbLogForward(log);
            }
        } else {
            // We check if this starts with the expected hash to determine if we want to absorb these changes or revert them.
            // NOTE:  We only revert a single block at a time (although that may contain multiple logs)!
            TransactionLog first = logs.get(0);
            if (this.nextPollingBlockNumber != first.blockNumber) {
                throw new IllegalArgumentException("consumeTransactionLogs() must be given logs corresponding to getPollingBlockNumber().");
            }
            if (this.expectedHash.equals(first.blockHash)) {
                // We are just continuing on the same history.
                for (TransactionLog log : logs) {
                    // Skip the overlap.
                    if (!first.blockHash.equals(log.blockHash)) {
                        absorbLogForward(log);
                    }
                }
            } else {
                // This is a reorganization so just revert logs until we find the previous block.
                while (!this.observedLogs.isEmpty() && this.expectedHash.equals(this.observedLogs.peek().blockHash)) {
                    TransactionLog logToRevert = this.observedLogs.pop();
                    this.listener.revertedLog(logToRevert);
                }
                if (this.observedLogs.isEmpty()) {
                    // We are reverting to the beginning.
                    this.nextPollingBlockNumber = this.startingBlockNumber;
                    this.expectedHash = null;
                } else {
                    TransactionLog top = this.observedLogs.peek();
                    this.nextPollingBlockNumber = top.blockNumber;
                    this.expectedHash = top.blockHash;
                }
            }
        }
    }


    private void absorbLogForward(TransactionLog log) {
        this.observedLogs.push(log);
        this.nextPollingBlockNumber = log.blockNumber;
        this.expectedHash = log.blockHash;
        this.listener.consumedLog(log);
    }
}
