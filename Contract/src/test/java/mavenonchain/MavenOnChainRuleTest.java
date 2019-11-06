package mavenonchain;

import avm.Address;
import java.nio.ByteBuffer;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.aion.types.AionAddress;
import org.aion.types.TransactionStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;

public class MavenOnChainRuleTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    //default address with balance
    private static BigInteger ENOUGH_BALANCE_TO_TRANSACT = BigInteger.TEN.pow(18 + 5);
    private static final BigInteger groupIDdeposit = new BigInteger("1000000000000000000000"); // 1000 AIONs
    private static Class[] otherClasses = {MavenOnChainEvents.class};
    private Address dappAddr;
    private Address from;

    @Before
    public void setup() {
        byte[] dapp = avmRule.getDappBytes(MavenOnChain.class, null, 1, otherClasses);
        from = avmRule.getRandomAddress(ENOUGH_BALANCE_TO_TRANSACT);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, dapp).getDappAddress();
    }

    @Test
    public void testGroupIdClaim() {
        //calling Dapps:
        // 1- encode method name and arguments
        // 2- make the call;
        String method = "claimGroupId";
        String groupId = "i.am.groot";
        byte[] txData = ABIUtil.encodeMethodArguments(method, groupId);
        BigInteger balanceBeforeCall = avmRule.kernel.getBalance(new AionAddress(from.toByteArray()));
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, groupIDdeposit, txData);

        // checks the status of the transaction execution
        TransactionStatus status = result.getReceiptStatus();
        Assert.assertTrue(status.isSuccess());

        BigInteger balanceAfterCall = avmRule.kernel.getBalance(new AionAddress(from.toByteArray()));
        Assert.assertEquals(
            balanceAfterCall.add(groupIDdeposit).add(BigInteger.valueOf(result.getTransactionResult().energyUsed)),
            balanceBeforeCall);

        // checks the log contains.
        Assert.assertFalse(result.getLogs().isEmpty());
        byte[] log0 = new byte[32];
        System.arraycopy(method.getBytes(), 0, log0, 0, method.getBytes().length);
        Assert.assertArrayEquals(log0, result.getLogs().get(0).copyOfTopics().get(0));
        Assert.assertArrayEquals(from.toByteArray(), result.getLogs().get(0).copyOfTopics().get(1));

        byte[] log2 = new byte[32];
        System.arraycopy(groupId.getBytes(), 0, log2, 0, groupId.getBytes().length);
        Assert.assertArrayEquals(log2, result.getLogs().get(0).copyOfTopics().get(2));
    }

    @Test
    public void testDeGroupIdClaim() {
        String method1 = "claimGroupId";
        String method2 = "deClaimGroupId";
        String groupId = "i.am.groot";
        byte[] txData = ABIUtil.encodeMethodArguments(method1, groupId);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, groupIDdeposit, txData);

        // checks the status of the transaction execution
        TransactionStatus status = result.getReceiptStatus();
        Assert.assertTrue(status.isSuccess());

        BigInteger balanceBeforeCall = avmRule.kernel.getBalance(new AionAddress(from.toByteArray()));

        txData = ABIUtil.encodeMethodArguments(method2, groupId);
        result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData);

        BigInteger balanceAfterCall = avmRule.kernel.getBalance(new AionAddress(from.toByteArray()));
        Assert.assertEquals(
            balanceAfterCall.subtract(groupIDdeposit).add(BigInteger.valueOf(result.getTransactionResult().energyUsed)),
            balanceBeforeCall);

        // checks the status of the transaction execution for the second call
        status = result.getReceiptStatus();
        Assert.assertTrue(status.isSuccess());

        // checks the log contains.
        Assert.assertFalse(result.getLogs().isEmpty());
        byte[] log0 = new byte[32];
        System.arraycopy(method2.getBytes(), 0, log0, 0, method2.getBytes().length);
        Assert.assertArrayEquals(log0, result.getLogs().get(0).copyOfTopics().get(0));
        Assert.assertArrayEquals(from.toByteArray(), result.getLogs().get(0).copyOfTopics().get(1));

        byte[] log2 = new byte[32];
        System.arraycopy(groupId.getBytes(), 0, log2, 0, groupId.getBytes().length);
        Assert.assertArrayEquals(log2, result.getLogs().get(0).copyOfTopics().get(2));
    }

    @Test
    public void testPublish() {
        String method1 = "claimGroupId";
        String method2 = "publish";
        String groupId = "i.am.groot";
        String artifact = "the.oan";
        String version = "1.0.0";
        String type = "jar";
        String cid = "cid";

        // claim groupId first
        byte[] txData = ABIUtil.encodeMethodArguments(method1, groupId);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, groupIDdeposit, txData);

        // getReceiptStatus() checks the status of the transaction execution
        TransactionStatus status = result.getReceiptStatus();
        Assert.assertTrue(status.isSuccess());

        // try to publish the maven
        txData = ABIUtil.encodeMethodArguments(method2, groupId, artifact, version, type, cid);
        result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData);

        // checks the status of the transaction execution again
        status = result.getReceiptStatus();
        Assert.assertTrue(status.isSuccess());

        // check the log contains.
        Assert.assertFalse(result.getLogs().isEmpty());
        byte[] log0 = new byte[32];
        System.arraycopy(method2.getBytes(), 0, log0, 0, method2.getBytes().length);
        Assert.assertArrayEquals(log0, result.getLogs().get(0).copyOfTopics().get(0));
        Assert.assertArrayEquals(from.toByteArray(), result.getLogs().get(0).copyOfTopics().get(1));

        byte[] log2 = new byte[32];
        System.arraycopy(groupId.getBytes(), 0, log2, 0, groupId.getBytes().length);
        Assert.assertArrayEquals(log2, result.getLogs().get(0).copyOfTopics().get(2));

        byte[] log3 = new byte[32];
        System.arraycopy(cid.getBytes(), 0, log3, 0, cid.getBytes().length);
        Assert.assertArrayEquals(log3, result.getLogs().get(0).copyOfTopics().get(3));

        ByteBuffer logData = ByteBuffer.allocate(artifact.getBytes().length + version.getBytes().length + type.getBytes().length + 3);
        logData.put((byte)artifact.getBytes().length)
            .put(artifact.getBytes())
            .put((byte)version.getBytes().length)
            .put(version.getBytes())
            .put((byte)type.getBytes().length)
            .put(type.getBytes());

        Assert.assertArrayEquals(logData.array(), result.getLogs().get(0).copyOfData());
    }
}

