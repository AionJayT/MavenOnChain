package mavenonipfs;

import avm.Blockchain;
import java.math.BigInteger;
import java.util.Arrays;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Fallback;

public class MavenOnIPFS
{
    private static final BigInteger groupIDdeposit = new BigInteger("1000000000000000000000"); // 1000 AIONs

    static {
        MavenOnIPFSEvents.mavenOnIPFSDeployed();
    }

    /**
     * Claim groupId from caller.
     *
     * @param groupId the Maven GroupID claim by the caller. The groupId can not exceed 32 chars.
     */
    @Callable
    public static void claimGroupId(String groupId) {
        Blockchain.require(Blockchain.getValue().equals(groupIDdeposit));
        Blockchain.require(groupId != null && groupId.length() <= 32);

        byte[] key = Blockchain.blake2b(groupId.getBytes());
        Blockchain.require(Blockchain.getStorage(key) == null);
        Blockchain.putStorage(key, Blockchain.getCaller().toByteArray());

        MavenOnIPFSEvents.claimGroupId(groupId);
    }

    /**
     * Declaim groupId from the caller.
     *
     * @param groupId the Maven GroupID declaim by the caller. The groupId can not exceed 32 chars.
     */
    @Callable
    public static void deClaimGroupId(String groupId) {
        Blockchain.require(groupId != null && groupId.length() <= 32);

        byte[] key = Blockchain.blake2b(groupId.getBytes());
        byte[] value = Blockchain.getStorage(key);
        Blockchain.require(value != null);

        Blockchain.require(Arrays.equals(Blockchain.getCaller().toByteArray(), value));
        Blockchain.putStorage(key, null);

        // Return the deposit to the caller
        Blockchain.call(Blockchain.getCaller(), groupIDdeposit, new byte[0], Blockchain.getRemainingEnergy());

        MavenOnIPFSEvents.deClaimGroupId(groupId);
    }

    /**
     * Publish the Maven dependency data and the cid from the caller, emit an event to the block.
     *
     * @param groupId the groupId of the maven dependency. The groupId can not exceed 32 chars.
     * @param artifactId the artifactId of the maven dependency. The artifactId can not exceed 32 chars.
     * @param version  the version of the maven dependency. The version can not exceed 32 chars.
     * @param type  the type of the maven dependency.
     * @param multihash the hash of the uploaded file given by the ipfs node. The multihash can not exceed 128 bytes.
     */
    @Callable
    public static void publish(String groupId, String artifactId, String version, byte type, byte[] multihash) {
        Blockchain.require(groupId != null && groupId.length() <= 32);
        Blockchain.require(artifactId != null && artifactId.length() <= 32);
        Blockchain.require(version != null && version.length() <= 32);
        Blockchain.require(multihash != null && multihash.length <= 128);

        byte[] key = Blockchain.blake2b(groupId.getBytes());
        byte[] value = Blockchain.getStorage(key);
        Blockchain.require(Arrays.equals(value, Blockchain.getCaller().toByteArray()));

        MavenOnIPFSEvents.publish(groupId, artifactId, version, type, multihash);
    }


    @Fallback
    public static void fallback(){
        Blockchain.revert();
    }
}
