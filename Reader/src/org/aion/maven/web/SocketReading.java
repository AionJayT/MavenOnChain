package org.aion.maven.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.aion.maven.state.MavenTuple;


/**
 * Just contains the helper to read the stream of the connected socket to find the request.
 * These helpers exist here and are all public to make for easier testing.
 */
public class SocketReading {
    private static final boolean VERBOSE = true;

    public static MavenTuple readRequest(InputStream stream) throws IOException {
        BufferedReader red = new BufferedReader(new InputStreamReader(stream));
        boolean keepReading = true;
        MavenTuple requestedTuple = null;
        while (keepReading) {
            String line = red.readLine();
            
            if (VERBOSE) {
                System.out.println("LINE: \"" + line + "\"");
            }
            if ((null == requestedTuple) && (line.length() > 0)) {
                // Parse this to see if it is a tuple.
                requestedTuple = attemptTupleParse(line);
            } else if (0 == line.length()) {
                // This is the end of the request.
                keepReading = false;
            } else {
                // We already parsed a tuple and this isn't an empty line so we are ignoring it.
            }
        }
        return requestedTuple;
    }

    public static MavenTuple attemptTupleParse(String line) {
        // Parse the string:  "GET /<GROUP_ID>/<ARTIFACT_ID>/<VERSION>/<ARTIFACT_ID>-<VERSION>.<TYPE> HTTP/1.1"
        MavenTuple tuple = null;
        if (line.startsWith("GET ")) {
            String[] spaces = line.split(" ");
            if (3 == spaces.length) {
                String path = spaces[1];
                String[] slashes = path.split("/");
                if (5 == slashes.length) {
                    String groupId = slashes[1];
                    String artifactId = slashes[2];
                    String version = slashes[3];
                    String file = slashes[4];
                    int dotIndex = file.lastIndexOf(".");
                    if (dotIndex >= 0) {
                        String type = file.substring(dotIndex + 1);
                        // Verify.
                        String construct = "GET /" + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "." + type + " HTTP/1.1";
                        if (line.equals(construct)) {
                            tuple = new MavenTuple(groupId, artifactId, version, type);
                        }
                    }
                }
            }
        }
        return tuple;
    }
}
