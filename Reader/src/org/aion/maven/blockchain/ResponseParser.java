package org.aion.maven.blockchain;

import java.util.ArrayList;
import java.util.List;

import org.aion.maven.types.Address;
import org.aion.maven.types.BlockHash;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


public class ResponseParser {
    public static List<TransactionLog> parseAsLogList(String jsonResponse) {
        JsonObject root = Json.parse(jsonResponse).asObject();
        JsonValue resultValue = root.get("result");
        
        List<TransactionLog> logs = null;
        if (null != resultValue) {
            logs = new ArrayList<>();
            for (JsonValue value : resultValue.asArray()) {
                JsonObject elt = value.asObject();
                Address sourceContractAddress = new Address(Codecs.hexStringToBytes(elt.get("address").asString()));
                byte[] data = Codecs.hexStringToBytes(elt.get("data").asString());
                JsonArray rawTopics = elt.get("topics").asArray();
                List<Topic> topics = new ArrayList<>();
                for (JsonValue rawTopic : rawTopics) {
                    // We expect topics to already be 32-bytes.
                    Topic topic = Topic.createFromPreciseData(Codecs.hexStringToBytes(rawTopic.asString()));
                    topics.add(topic);
                }
                long blockNumber = Codecs.hexStringToLong(elt.get("blockNumber").asString());
                BlockHash blockHash = new BlockHash(Codecs.hexStringToBytes(elt.get("blockHash").asString()));
                int transactionIndex = Codecs.hexStringToInteger(elt.get("transactionIndex").asString());
                int logIndex = Codecs.hexStringToInteger(elt.get("logIndex").asString());
                TransactionLog oneLog = new  TransactionLog(sourceContractAddress, data, topics, blockNumber, blockHash, transactionIndex, logIndex);
                logs.add(oneLog);
            }
        }
        return logs;
    }
}
