package mavenonipfs.publisher;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Publisher {

    private static final int ipfsRequestTimeout = 10000; // 10sec

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("No argument input, check the help by execute \" --h\"");
            return;
        }

        switch (args[0]) {
            case "--h":
            case "-help":
                printHelp();
                break;
            case "--p":
            case "-publish":
                if (args.length > 1 && args[1] != null) {
                    publish(args[1], args.length == 2 ? null : args[2]);
                } else {
                    System.out.println("Invalid arguments input, please check the help");
                }
            break;
            default:
                printHelp();
        }
    }

    static String publish(String fileName, String ipfsAddress) {
        try {
            Path path = Paths.get(System.getProperty("user.dir") + "/"+ fileName);

            IPFS ipfs = new IPFS(ipfsAddress == null ? "/ip4/127.0.0.1/tcp/5001" : ipfsAddress);
            ipfs.timeout(ipfsRequestTimeout);

            NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(path.toFile());
            MerkleNode result = ipfs.add(file).get(0);
            byte[] hash = result.hash.toBytes();

            String hexString = String.format("%0" + (hash.length * 2) + "x", new BigInteger(1, hash));
            System.out.println(hexString);
            return hexString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printHelp() {
        System.out.println("publisher [app options] <fileName>");
        System.out.println("app options");
        System.out.println("  -help, --h                print this message and publisher help");
        System.out.println("  -publish, --p <fileName>  publish the file into the IPFS");
    }
}
