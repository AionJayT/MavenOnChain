import org.aion.maven.blockchain.BlockchainReader;
import org.aion.maven.blockchain.Codecs;
import org.aion.maven.blockchain.Rpc;
import org.aion.maven.state.ProjectedState;
import org.aion.maven.types.Address;
import org.aion.maven.web.WebServer;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;


public class Reader {
    public static void main(String[] args) throws Throwable {
        String kernelHostname = null;
        int kernelPort = -1;
        String ipfsHostname = null;
        int ipfsPort = -1;
        int port = -1;
        Address contractAddress = null;
        long startingBlockNumber = -1L;
        try {
            kernelHostname = parseOption(args, "--kernelHostname");
            kernelHostname.toString();
            kernelPort = Integer.parseInt(parseOption(args, "--kernelPort"));
            ipfsHostname = parseOption(args, "--ipfsHostname");
            ipfsHostname.toString();
            ipfsPort = Integer.parseInt(parseOption(args, "--ipfsPort"));
            port = Integer.parseInt(parseOption(args, "--listenPort"));
            contractAddress = new Address(Codecs.hexStringToBytes(parseOption(args, "--contractAddress")));
            startingBlockNumber = Long.parseLong(parseOption(args, "--startingBlockNumber"));
        } catch (Exception e) {
            System.err.println("Usage:  Reader <options>"
                    + "\n\t--kernelHostname kernelHostname - The hostname/IP of the Aion kernel"
                    + "\n\t--kernelPort kernelPort - The RPC port of the Aion kernel"
                    + "\n\t--ipfsHostname ipfsHostname - The hostname/IP of the IPFS node"
                    + "\n\t--ipfsPort ipfsPort - The port of the IPFS node"
                    + "\n\t--listenPort listenPort - The port this web server will use"
                    + "\n\t--contractAddress contractAddress - The address of the contract to observe"
                    + "\n\t--startingBlockNumber startingBlockNumber - The block where the read should start"
            );
            System.exit(1);
        }
        
        // Create all the components.
        ProjectedState<Multihash> projection = new ProjectedState<>();
        Rpc rpc = new Rpc(kernelHostname, kernelPort);
        IPFS ipfs = new IPFS(ipfsHostname, ipfsPort);
        BlockchainReader reader = new BlockchainReader(ipfs, projection, rpc, contractAddress, startingBlockNumber);
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


    private static String parseOption(String[] args, String option) {
        String result = null;
        for (int i = 0; i < args.length; ++i) {
            if (option.equals(args[i]) && ((i + 1) < args.length)) {
                result = args[i+1];
            }
        }
        return result;
    }
}
