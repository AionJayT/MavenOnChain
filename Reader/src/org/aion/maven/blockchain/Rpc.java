package org.aion.maven.blockchain;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aion.maven.types.Address;
import org.aion.maven.types.Topic;
import org.aion.maven.types.TransactionLog;


/**
 * Note that the ideas here are largely copied from the node_test_harness's org.aion.harness.main.RPC and org.aion.harness.main.tools.RpcCaller.
 */
public class Rpc {
    private final String hostname;
    private final int port;
    private final HttpClient httpClient;

    public Rpc(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    public Result<List<TransactionLog>> getLatestFilteredLogs(long startingPoint, Set<Topic> firstTopicFilterUnion, Address reportingContract) {
        if (startingPoint < 1) {
            throw new IllegalArgumentException("Starting point must be a positive integer.");
        }
        if (reportingContract == null) {
            throw new IllegalArgumentException("This helper expects that the address is already known.");
        }

        // Construct the payload to the rpc call (ie. the content of --data).
        // For now, we will just use "latest" as the destination but non-testing cases will likely want to impose a limit on this.
        String payload = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getLogs\",\"params\":[{\"fromBlock\":" + startingPoint + ",\"toBlock\":\"latest\"";
        if (null != firstTopicFilterUnion) {
            payload += ",\"topics\":[[";
            boolean addComma = false;
            if (firstTopicFilterUnion.isEmpty()) {
                throw new IllegalArgumentException("The set of first topic filters must be null or non-empty.");
            }
            for (Topic topic : firstTopicFilterUnion) {
                if (addComma) {
                    payload += ",";
                }
                payload += "\"" + topic.renderedAsHexNumber() + "\"";
                addComma = true;
            }
            payload += "]]";
        }
        payload += "}],\"id\":1}";

        boolean verbose = true;
        String jsonResponse = callWithPayload(payload, verbose);
        List<TransactionLog> logList = null;
        if (null != jsonResponse) {
            List<TransactionLog> rawList = ResponseParser.parseAsLogList(jsonResponse);
            // Filter the list by those associated with this address.
            logList = rawList.stream().filter((log) -> reportingContract.equals(log.sourceContractAddress)).collect(Collectors.toList());
        }
        return (null != logList)
                ? new Result<List<TransactionLog>>(true, logList)
                : new Result<List<TransactionLog>>(false, null);
    }



    private String callWithPayload(String payload, boolean verbose) {
        URI uri = URI.create("http://" + this.hostname + ":" + this.port);
        if (verbose) {
            System.out.println("Sending to " + uri + ": <payload>" + payload + "</payload>");
        }
        // We just want to send the entire payload as the data to the POST, with no additional variables and only the content-type header.
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .uri(uri)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = null;
        try {
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // We see this on connection refused, etc.
            response = null;
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interrupt while contacting RPC URI:  " + uri, e);
        }
        
        String resultJson = null;
        if (null != response) {
            int statusCode = response.statusCode();
            String output = response.body();
            if (verbose) {
                System.out.println("Received " + statusCode + ": <response>" + output + "</response>");
            }

            if ((null != output) && (200 == statusCode) && !output.isEmpty()) {
                resultJson = output;
            }
        } else {
            if (verbose) {
                System.out.println("Failed to get <response/>");
            }
        }
        return resultJson;
    }


    public static final class Result<T> {
        public final boolean isSuccess;
        public final T result;
        
        public Result(boolean isSuccess, T result) {
            this.isSuccess = isSuccess;
            this.result = result;
        }
    }
}
