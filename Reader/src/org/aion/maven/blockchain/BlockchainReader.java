package org.aion.maven.blockchain;

import org.aion.maven.state.ProjectedState;
import org.aion.maven.types.Address;
import org.aion.maven.types.TransactionLog;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;


/**
 * The part of the system responsible for polling the event log of the blockchain, writing data to the ProjectedState, and pinning resources on the IPFS node.
 * TODO:  Implement this when adding IPFS support.
 */
public class BlockchainReader implements ILogStateListener {
    public BlockchainReader(IPFS ipfs, ProjectedState<Multihash> projection, Rpc blockchainRpc, Address reportingContract, long startingPoint) {
        // TODO Auto-generated constructor stub
    }

    public void start() {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void consumedLog(TransactionLog log) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revertedLog(TransactionLog log) {
        // TODO Auto-generated method stub
        
    }
}
