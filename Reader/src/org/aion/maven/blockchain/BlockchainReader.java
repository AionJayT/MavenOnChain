package org.aion.maven.blockchain;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.aion.maven.blockchain.Rpc.Result;
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
    private static final Topic CLAIM_GROUP_ID = Topic.createFromArbitraryData("claimGroupId".getBytes());
    private static final Topic DECLAIM_GROUP_ID = Topic.createFromArbitraryData("deClaimGroupId".getBytes());
    private static final Set<Topic> FIRST_TOPIC_FILTER_UNION = Set.of(CLAIM_GROUP_ID, DECLAIM_GROUP_ID, PublishEvent.PUBLISH);

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
        PublishEvent event = readAsPublishEvent(log);
        if (null != event) {
            // Pin the element.
            try {
                this.ipfs.pin.add(event.ipfsMultihash);
            } catch (IOException e) {
                throw new AssertionError("TODO:  Handle IOException once their cause is defined: ", e);
            }
            // Update this in the projected state.
            this.projection.writeReference(event.mavenTuple, event.ipfsMultihash);
        } else {
            System.out.println("Ignored event: " + log);
        }
    }

    @Override
    public void revertedLog(TransactionLog log) {
        PublishEvent event = readAsPublishEvent(log);
        if (null != event) {
            // Remove this from the projected state.
            this.projection.clearReference(event.mavenTuple);
            // Unpin the element.
            try {
                this.ipfs.pin.rm(event.ipfsMultihash);
            } catch (IOException e) {
                throw new AssertionError("TODO:  Handle IOException once their cause is defined: ", e);
            }
        } else {
            System.out.println("Ignored revert event: " + log);
        }
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

    private PublishEvent readAsPublishEvent(TransactionLog log) {
        Topic name = log.topics.get(0);
        PublishEvent event = null;
        // For now, we are going to ignore claim/declaim.
        if (name.equals(CLAIM_GROUP_ID)) {
            event = null;
            System.out.println("CLAIM: " + log.topics.get(3).extractAsNullTerminatedString());
        } else if (name.equals(DECLAIM_GROUP_ID)) {
            event = null;
            System.out.println("DECLAIM: " + log.topics.get(3).extractAsNullTerminatedString());
        } else if (name.equals(PublishEvent.PUBLISH)) {
            event = PublishEvent.readFromLog(log);
            System.out.println("PUBLISH: "
                    + event.mavenTuple.groupId+ " "
                    + event.mavenTuple.artifactId+ " "
                    + event.mavenTuple.version + "."
                    + event.mavenTuple.type + " -> "
                    + event.ipfsMultihash.toBase58()
            );
        } else {
            throw new AssertionError("UNKNOWN TOPIC: " + name);
        }
        return event;
    }
}
