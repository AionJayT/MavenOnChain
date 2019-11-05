package org.aion.maven.web;

import java.io.IOException;
import java.net.ServerSocket;

import org.aion.maven.state.ProjectedState;


/**
 * For now, we will just run this on a single thread.
 */
public class WebServer {
    private final ProjectedState projection;
    // We will close the thread's descriptors on shutdown.
    private WebThread thread;

    public WebServer(ProjectedState projection) {
        this.projection = projection;
    }

    public void start(int port) throws IOException {
        if (null != this.thread) {
            throw new AssertionError("Server already running");
        }
        ServerSocket server = new ServerSocket(2000);
        this.thread = new WebThread(this.projection, server);
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
