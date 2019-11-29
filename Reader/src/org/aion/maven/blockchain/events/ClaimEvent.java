package org.aion.maven.blockchain.events;

import org.aion.maven.types.Address;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;


/**
 * This class just exists to contain the logic for parsing the TransactionLog into something we can use.
 * See AKI-520 for canonical details of the encoding of this event.
 * Summary:
 * -Topic 1:  (String) "claimGroupId"
 * -Topic 2:  (byte[]) Caller address
 * -Topic 3:  (String) groupId
 */
public class ClaimEvent {
    public static final Topic CLAIM_GROUP_ID = Topic.createFromArbitraryData("claimGroupId".getBytes());

    public static ClaimEvent readFromLog(TransactionLog log) {
        if (3 != log.topics.size()) {
            throw new IllegalArgumentException("Must contain 3 topics");
        }
        if (!CLAIM_GROUP_ID.equals(log.topics.get(0))) {
            throw new IllegalArgumentException("Can only be called with claim logs");
        }
        Address callerAddress = log.topics.get(1).readAsAddress();
        String groupId = log.topics.get(2).extractAsNullTerminatedString();
        return new ClaimEvent(callerAddress, groupId);
    }


    public final Address callerAddress;
    public final String groupId;
    private ClaimEvent(Address callerAddress, String groupId) {
        this.callerAddress = callerAddress;
        this.groupId = groupId;
    }
}
