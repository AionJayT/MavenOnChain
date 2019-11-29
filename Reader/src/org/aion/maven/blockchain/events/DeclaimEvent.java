package org.aion.maven.blockchain.events;

import org.aion.maven.types.Address;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;


/**
 * This class just exists to contain the logic for parsing the TransactionLog into something we can use.
 * See AKI-520 for canonical details of the encoding of this event.
 * Summary:
 * -Topic 1:  (String) "deClaimGroupId"
 * -Topic 2:  (byte[]) Caller address
 * -Topic 3:  (String) groupId
 */
public class DeclaimEvent {
    public static final Topic DECLAIM_GROUP_ID = Topic.createFromArbitraryData("deClaimGroupId".getBytes());

    public static DeclaimEvent readFromLog(TransactionLog log) {
        if (3 != log.topics.size()) {
            throw new IllegalArgumentException("Must contain 3 topics");
        }
        if (!DECLAIM_GROUP_ID.equals(log.topics.get(0))) {
            throw new IllegalArgumentException("Can only be called with declaim logs");
        }
        Address callerAddress = log.topics.get(1).readAsAddress();
        String groupId = log.topics.get(2).extractAsNullTerminatedString();
        return new DeclaimEvent(callerAddress, groupId);
    }


    public final Address callerAddress;
    public final String groupId;
    private DeclaimEvent(Address callerAddress, String groupId) {
        this.callerAddress = callerAddress;
        this.groupId = groupId;
    }
}
