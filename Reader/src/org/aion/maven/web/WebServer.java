package org.aion.maven.web;

import java.io.IOException;
import java.net.ServerSocket;

import org.aion.maven.state.ProjectedState;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;


/**
 * For now, we will just run this on a single thread.
 */
public class WebServer {
    private final IPFS ipfs;
    private final ProjectedState<Multihash> projection;
    // We will close the thread's descriptors on shutdown.
    private WebThread thread;

    public WebServer(IPFS ipfs, ProjectedState<Multihash> projection) {
        this.ipfs = ipfs;
        this.projection = projection;
    }

    public void start(int port) throws IOException {
        if (null != this.thread) {
            throw new AssertionError("Server already running");
        }
        ServerSocket server = new ServerSocket(port);
        this.thread = new WebThread(this.ipfs, this.projection, server);
        this.thread.start();
    }

    public void stop() throws IOException {
        if (null == this.thread) {
            throw new AssertionError("Server not running");
        }
        this.thread.signalExit();
        try {
            this.thread.join();
        } catch (InterruptedException e) {
            throw new AssertionError("Interruption not expected", e);
        }
    }
}
