package mavenonchain;

import avm.Blockchain;
import org.aion.avm.userlib.AionBuffer;

class MavenOnChainEvents {
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

    static void publish (String groupId, String artifactId, String version, String type, String cid) {
        byte[] bytesArtifactId = artifactId.getBytes();
        byte[] bytesVersion = version.getBytes();
        byte[] bytesType = type.getBytes();

        AionBuffer buffer = AionBuffer.allocate(3 + bytesArtifactId.length + bytesVersion.length + bytesType.length);
        buffer.putByte((byte) bytesArtifactId.length)
            .put(bytesArtifactId)
            .putByte((byte) bytesVersion.length)
            .put(bytesVersion)
            .putByte((byte) bytesType.length)
            .put(bytesType);

        Blockchain.log("publish".getBytes(),
            Blockchain.getCaller().toByteArray()
            , groupId.getBytes()
            , cid.getBytes()
            , buffer.getArray());
    }

    static void mavenOnChainDeployed() {
        Blockchain.log("MavenOnChainDeployed".getBytes(),
            Blockchain.getCaller().toByteArray()
            , new byte[0]);
    }
}
