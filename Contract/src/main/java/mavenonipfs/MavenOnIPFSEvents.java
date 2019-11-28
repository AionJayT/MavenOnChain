package mavenonipfs;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

class MavenOnIPFSEvents {
    static void mavenOnIPFSDeployed() {
        Blockchain.log("Maven on IPFS deployed".getBytes()
            , new byte[0]);
    }

    static void claimGroupId (String groupId) {
        Blockchain.log("claimGroupId".getBytes(),
            Blockchain.getCaller().toByteArray()
            , groupId.getBytes()
            , new byte[0]);
    }

    static void deClaimGroupId (String groupId) {
        Blockchain.log("deClaimGroupId".getBytes(),
            Blockchain.getCaller().toByteArray()
            , groupId.getBytes()
            , new byte[0]);
    }

    static void publish (String groupId, String artifactId, String version,  byte type, byte[] multihash) {
        byte[] data = new ABIStreamingEncoder()
            .encodeOneString(version)
            .encodeOneByte(type)
            .encodeOneByteArray(multihash)
            .toBytes();

        Blockchain.log("publish".getBytes()
            , groupId.getBytes()
            , artifactId.getBytes()
            , data);
    }
}
