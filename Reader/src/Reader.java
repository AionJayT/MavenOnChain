import org.aion.maven.blockchain.BlockchainReader;
import org.aion.maven.state.ProjectedState;
import org.aion.maven.web.WebServer;

import io.ipfs.api.IPFS;


public class Reader {
    public static void main(String[] args) throws Throwable {
        // For now, the port is just hard-coded.
        int port = 2000;
        
        // Create all the components.
        ProjectedState projection = new ProjectedState();
        BlockchainReader reader = new BlockchainReader(projection);
        IPFS ipfs = new IPFS("localhost", 5001);
        WebServer server = new WebServer(ipfs, projection);
        
        // Start up the components.
        server.start(port);
        reader.start();
        System.out.println("Reader running on port: " + port);
        
        // Just sleep forever (we will rely on Ctrl-C).
        boolean keepRunning = true;
        while (keepRunning) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // We aren't using interruption in this application.
                throw new RuntimeException(e);
            }
            keepRunning = false;
        }
        
        // For completeness (even though we don't currently hit this), stop the components.
        System.out.println("Shutting down reader...");
        reader.stop();
        server.stop();
    }
}
