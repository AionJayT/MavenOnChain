package mavenonchain;

import avm.Blockchain;
import java.math.BigInteger;
import java.util.Arrays;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Fallback;

public class MavenOnChain
{
    private static final BigInteger groupIDdeposit = new BigInteger("1000000000000000000000"); // 1000 AIONs

    static {
        MavenOnChainEvents.mavenOnChainDeployed();
    }

    /**
     * Claim groupId from caller.
     *
     * @param groupid the Maven GroupID claim by cthe aller.
     */
    @Callable
    public static void claimGroupId(String groupid) {
        Blockchain.require(Blockchain.getValue().equals(groupIDdeposit));
        Blockchain.require(groupid != null);

        byte[] key = Blockchain.blake2b(groupid.getBytes());
        Blockchain.require(Blockchain.getStorage(key) == null);
        Blockchain.putStorage(key, Blockchain.getCaller().toByteArray());

        MavenOnChainEvents.claimGroupId(groupid);
    }

    /**
     * Declaim groupId from the caller.
     *
     * @param groupId the Maven GroupID declaim by the caller.
     */
    @Callable
    public static void deClaimGroupId(String groupId) {
        Blockchain.require(groupId != null);

        byte[] key = Blockchain.blake2b(groupId.getBytes());
        byte[] value = Blockchain.getStorage(key);
        Blockchain.require(value != null);

        Blockchain.require(Arrays.equals(Blockchain.getCaller().toByteArray(), value));
        Blockchain.putStorage(key, null);

        // Return the deposit to the caller
        Blockchain.call(Blockchain.getCaller(), groupIDdeposit, new byte[0], Blockchain.getRemainingEnergy());

        MavenOnChainEvents.deClaimGroupId(groupId);
    }

    /**
     * Publish the Maven dependency data and the cid from the caller, emit an event to the block.
     *
     * @param groupId the groupId of the maven dependency.
     * @param artifactId the artifactId of the maven dependency.
     * @param version  the version of the maven dependency.
     * @param type  the type of the maven dependency.
     * @param cid the Maven GroupID claimed by caller.
     */
    @Callable
    public static void publish(String groupId, String artifactId, String version, String type, String cid) {
        Blockchain.require(groupId != null);
        Blockchain.require(artifactId != null);
        Blockchain.require(version != null);
        Blockchain.require(type != null);
        Blockchain.require(cid != null);

        byte[] key = Blockchain.blake2b(groupId.getBytes());
        byte[] value = Blockchain.getStorage(key);
        Blockchain.require(Arrays.equals(value, Blockchain.getCaller().toByteArray()));

        MavenOnChainEvents.publish(groupId, artifactId, version, type, cid);
    }


    @Fallback
    public static void fallback(){
        Blockchain.revert();
    }
}
