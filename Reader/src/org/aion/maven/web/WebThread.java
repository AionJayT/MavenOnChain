package org.aion.maven.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.aion.maven.state.IpfsReference;
import org.aion.maven.state.MavenTuple;
import org.aion.maven.state.ProjectedState;


/**
 * The thread actually running in the WebServer.
 * This is mostly just split out for testing and clarity reasons.
 */
public class WebThread extends Thread {
    private final ProjectedState projection;
    private ServerSocket server;
    private Socket currentClient;

    public WebThread(ProjectedState projection, ServerSocket server) {
        this.projection = projection;
        this.server = server;
    }

    public synchronized void signalExit() throws IOException {
        this.server.close();
        this.server = null;
        if (null != this.currentClient) {
            this.currentClient.close();
            this.currentClient = null;
        }
    }

    private synchronized boolean setCurrentClient(Socket client) {
        boolean stillRunning = (null != this.server);
        if (stillRunning) {
            this.currentClient = client;
        }
        return stillRunning;
    }

    @Override
    public void run() {
        // If we are to be asynchronously shutdown, it will be by closing one of our sockets.
        try {
            while (true) {
                Socket sock = this.server.accept();
                boolean stillRunning = setCurrentClient(sock);
                if (stillRunning) {
                    InputStream ipfsStream = null;
                    MavenTuple requestedTuple = SocketReading.readRequest(sock.getInputStream());
                    if (null != requestedTuple) {
                        // Resolve this tuple as data and pipe it back to the caller.
                        ipfsStream = resolveIpfsDataStream(requestedTuple);
                    }
                    if (null != ipfsStream) {
                        // Pipe the data back as a 200.
                        // We "opened" this stream by resolveIpfsDataStream, so close it.
                        ipfsStream.close();
                    } else {
                        // Not found.
                        String httpResponse = "HTTP/1.1 404 NOT FOUND\r\n\r\n";
                        sock.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                    }
                }
                sock.close();
            }
        } catch (IOException e) {
            // This could happen on shutdown so check to see if we are still running while shutting down.
            boolean stillRunning = setCurrentClient(null);
            if (stillRunning) {
                System.err.println("UNEXPECTED EXCEPTION");
                e.printStackTrace();
            }
        }
    }

    private InputStream resolveIpfsDataStream(MavenTuple requestedTuple) {
        InputStream dataStream = null;
        // 1) Resolve this to an IPFS CID.
        IpfsReference reference = this.projection.resolveReference(requestedTuple);
        // 2) Ask for the data stream from this CID.
        if (null != reference) {
            // TODO:  Implement when IPFS added.
        }
        return dataStream;
    }
}
