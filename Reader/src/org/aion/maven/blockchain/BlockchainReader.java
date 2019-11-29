package org.aion.maven.blockchain;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.aion.maven.blockchain.Rpc.Result;
import org.aion.maven.blockchain.events.ClaimEvent;
import org.aion.maven.blockchain.events.DeclaimEvent;
import org.aion.maven.blockchain.events.PublishEvent;
import org.aion.maven.state.ProjectedState;
import org.aion.maven.types.Address;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;


/**
 * The part of the system responsible for polling the event log of the blockchain, writing data to the ProjectedState, and pinning resources on the IPFS node.
 */
public class BlockchainReader implements ILogStateListener {
    // 1 second is horribly impractical but makes testing/demonstration easier.
    private static final long POLL_FREQUENCY_MILLIS = 1_000L;
    // (topics based on documentation in AKI-520).
    private static final Topic DECLAIM_GROUP_ID = Topic.createFromArbitraryData("deClaimGroupId".getBytes());
    private static final Set<Topic> FIRST_TOPIC_FILTER_UNION = Set.of(ClaimEvent.CLAIM_GROUP_ID, DECLAIM_GROUP_ID, PublishEvent.PUBLISH);

    private final IPFS ipfs;
    private final ProjectedState<Multihash> projection;
    private final Rpc blockchainRpc;
    private final Address reportingContract;
    private final LogReaderStateMachine stateMachine;

    private Thread pollingThread;
    private boolean keepRunning;

    public BlockchainReader(IPFS ipfs, ProjectedState<Multihash> projection, Rpc blockchainRpc, Address reportingContract, long startingPoint) {
        this.ipfs = ipfs;
        this.projection = projection;
        this.blockchainRpc = blockchainRpc;
        this.reportingContract = reportingContract;
        this.stateMachine = new LogReaderStateMachine(this, startingPoint);
    }

    public void start() {
        if (null != this.pollingThread) {
            throw new AssertionError("Thread already started");
        }
        this.pollingThread = new Thread() {
            @Override
            public void run() {
                backgroundRunThreadLoop();
            }
        };
        this.pollingThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        this.keepRunning = true;
        this.pollingThread.start();
    }

    public void stop() {
        synchronized(this) {
            this.keepRunning = false;
            this.notifyAll();
        }
        try {
            this.pollingThread.join();
        } catch (InterruptedException e) {
            throw new AssertionError("Interruption not used", e);
        }
        this.pollingThread = null;
    }

    @Override
    public void consumedLog(TransactionLog log) {
        readAndProcessEvent(log, (publishEvent) -> {
            // Pin the element.
            try {
                this.ipfs.pin.add(publishEvent.ipfsMultihash);
            } catch (IOException e) {
                throw new AssertionError("TODO:  Handle IOException once their cause is defined: ", e);
            }
            // Update this in the projected state.
            this.projection.writeReference(publishEvent.mavenTuple, publishEvent.ipfsMultihash);
        }, (claimEvent) -> {
            
        }, (declaimEvent) -> {
            
        });
    }

    @Override
    public void revertedLog(TransactionLog log) {
        readAndProcessEvent(log, (publishEvent) -> {
            // Remove this from the projected state.
            this.projection.clearReference(publishEvent.mavenTuple);
            // Unpin the element.
            try {
                this.ipfs.pin.rm(publishEvent.ipfsMultihash);
            } catch (IOException e) {
                throw new AssertionError("TODO:  Handle IOException once their cause is defined: ", e);
            }
        }, (claimEvent) -> {
            // Just remove this from the set of valid groups.
            this.projection.removeClaim(claimEvent.groupId);
        }, (declaimEvent) -> {
            // Just re-add this from the set of valid groups.
            this.projection.addClaim(declaimEvent.groupId);
        });
    }


    private void backgroundRunThreadLoop() {
        // We don't need a work item, just a delay, so we will start assuming we want to do work.
        boolean doOneIteration = true;
        while (doOneIteration) {
            long nextPollStartBlockNumber = this.stateMachine.getPollingBlockNumber();
            Result<List<TransactionLog>> result = this.blockchainRpc.getLatestFilteredLogs(nextPollStartBlockNumber, FIRST_TOPIC_FILTER_UNION, this.reportingContract);
            if (result.isSuccess) {
                // Process updates to the transaction log.
                this.stateMachine.consumeTransactionLogs(result.result);
            } else {
                // TODO:  This is probably a network thing we should just log but failing makes for an easier test.
                throw new RuntimeException("ERROR GETTING FILTERED LOGS!");
            }
            doOneIteration = delayUntilNextPoll();
        }
    }

    private synchronized boolean delayUntilNextPoll() {
        long currentMillis = System.currentTimeMillis();
        long endMillis = currentMillis + POLL_FREQUENCY_MILLIS;
        while (this.keepRunning && (currentMillis < endMillis)) {
            long waitMillis = endMillis - currentMillis;
            try {
                this.wait(waitMillis);
            } catch (InterruptedException e) {
                throw new AssertionError("Interruption not used", e);
            }
            currentMillis = System.currentTimeMillis();
        }
        return this.keepRunning;
    }

    private void readAndProcessEvent(TransactionLog log, Consumer<PublishEvent> publishConsumer, Consumer<ClaimEvent> claimConsumer, Consumer<DeclaimEvent> declaimConsumer) {
        Topic name = log.topics.get(0);
        if (name.equals(PublishEvent.PUBLISH)) {
            publishConsumer.accept(PublishEvent.readFromLog(log));
        } else if (name.equals(ClaimEvent.CLAIM_GROUP_ID)) {
            claimConsumer.accept(ClaimEvent.readFromLog(log));
        } else if (name.equals(DeclaimEvent.DECLAIM_GROUP_ID)) {
            declaimConsumer.accept(DeclaimEvent.readFromLog(log));
        } else {
            throw new AssertionError("UNKNOWN TOPIC: " + name);
        }
    }
}
