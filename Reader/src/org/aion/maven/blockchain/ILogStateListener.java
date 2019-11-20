package org.aion.maven.blockchain;

import org.aion.maven.types.TransactionLog;


/**
 * Receives callbacks from the LogReaderStateMachine as it discovers changes to the blockchain history.
 * Note that these will always be passed in a symmetric order, on revert (that is, consume A, B, C, revert C, B, A).
 * Also, the same log will never be consumed/reverted twice, unless the blockchain reorganizes multiple times.
 * 
 * Note that the current in-memory representation of the LogReaderStateMachine's history means that a log sent to revertedLog
 * will always be an instance formerly passed to consumedLog but this may not be reliable, in the future.
 */
public interface ILogStateListener {
    /**
     * Called to tell the receiver that the LogReaderStateMachine has consumed the given log.
     * 
     * @param log The log which was just consumed.
     */
    void consumedLog(TransactionLog log);

    /**
     * Called to tell the receiver that the LogReaderStateMachine has reverted the given log.
     * 
     * @param log The log which was just reverted.
     */
    void revertedLog(TransactionLog log);
}
