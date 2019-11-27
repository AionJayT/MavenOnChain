package org.aion.maven.blockchain.events;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.maven.state.MavenTuple;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;

import io.ipfs.multihash.Multihash;


/**
 * This class just exists to contain the logic for parsing the TransactionLog into something we can use.
 * See AKI-520 for canonical details of the encoding of this event.
 * Summary:
 * -Topic 1:  (String) "publish"
 * -Topic 2:  (String) groupId
 * -Topic 3:  (String) artifactId
 * -data:  ABIEncoded:  (String) version, (byte) type, (String) base58 MultiHash
 * (note that type 0 = pom, 1 = jar)
 */
public class PublishEvent {
    public static final Topic PUBLISH = Topic.createFromArbitraryData("publish".getBytes());

    public static PublishEvent readFromLog(TransactionLog log) {
        if (3 != log.topics.size()) {
            throw new IllegalArgumentException("Must contain 3 topics");
        }
        if (!PUBLISH.equals(log.topics.get(0))) {
            throw new IllegalArgumentException("Can only be called with publish logs");
        }
        String groupId = log.topics.get(1).extractAsNullTerminatedString();
        String artifactId = log.topics.get(2).extractAsNullTerminatedString();
        ABIDecoder decoder = new ABIDecoder(log.data);
        String version = decoder.decodeOneString();
        byte type = decoder.decodeOneByte();
        if ((0 != type) && (1 != type)) {
            throw new IllegalArgumentException("Undefined type");
        }
        String typeName = (0 == type)
                ? "pom"
                : "jar";
        String cid = decoder.decodeOneString();
        MavenTuple mavenTuple = new MavenTuple(groupId, artifactId, version, typeName);
        // TODO:  Change the cid to just a binary encoding instead of a String rendering.
        Multihash ipfsMultihash = Multihash.fromBase58(cid);
        return new PublishEvent(mavenTuple, ipfsMultihash);
    }


    public final MavenTuple mavenTuple;
    public final Multihash ipfsMultihash;
    private PublishEvent(MavenTuple mavenTuple, Multihash ipfsMultihash) {
        this.mavenTuple = mavenTuple;
        this.ipfsMultihash = ipfsMultihash;
    }
}
