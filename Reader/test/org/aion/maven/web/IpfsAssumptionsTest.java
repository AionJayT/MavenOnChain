package org.aion.maven.web;

import java.net.SocketTimeoutException;

import org.junit.Assert;
import org.junit.Test;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;


/**
 * These tests require access to an IPFS node.
 * For whatever reason, the IPFS world typically uses base58 hashes so these are references to tutorials and public references.
 */
public class IpfsAssumptionsTest {
    private static final String IPFS_HOSTNAME = "localhost";
    private static final int IPFS_PORT = 5001;
    private static final int IPFS_TIMEOUT_MILLIS = 1_000;

    @Test
    public void testFileFound() throws Exception {
        IPFS ipfs = new IPFS(IPFS_HOSTNAME, IPFS_PORT).timeout(IPFS_TIMEOUT_MILLIS);
        
        String hash = "QmT78zSuBmuS4z925WZfrqQ1qHaJ56DQaTfyMUF7F8ff5o";
        Multihash multihash = Multihash.fromBase58(hash);
        long start = System.currentTimeMillis();
        boolean didTimeout = false;
        try {
            ipfs.catStream(multihash).close();
        } catch (SocketTimeoutException e) {
            didTimeout = true;
        }
        long end = System.currentTimeMillis();
        Assert.assertTrue(!didTimeout);
        long deltaMillis = end - start;
        Assert.assertTrue(deltaMillis <= (long)IPFS_TIMEOUT_MILLIS);
    }

    @Test
    public void testFileNotFound() throws Exception {
        IPFS ipfs = new IPFS(IPFS_HOSTNAME, IPFS_PORT).timeout(IPFS_TIMEOUT_MILLIS);
        
        String hash = "QmT78zSuBmuS4z925WZfrqQ1qHaJ56DQaTfyMUF7F8ff6o";
        Multihash multihash = Multihash.fromBase58(hash);
        long start = System.currentTimeMillis();
        boolean didTimeout = false;
        try {
            ipfs.catStream(multihash).close();
        } catch (SocketTimeoutException e) {
            didTimeout = true;
        }
        long end = System.currentTimeMillis();
        Assert.assertTrue(didTimeout);
        long deltaMillis = end - start;
        Assert.assertTrue(deltaMillis >= (long)IPFS_TIMEOUT_MILLIS);
    }
}
