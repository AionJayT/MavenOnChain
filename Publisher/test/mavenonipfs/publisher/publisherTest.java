package mavenonipfs.publisher;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class publisherTest {

    private static String cid;
    private IPFS ipfs;
    private String testFileName = "example.txt";

    @Before
    public void setup() {
        ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
        ipfs.timeout(1000);
    }

    @After
    public void tearDown() {
        try {
            // Remove the file
            List<Multihash> result = ipfs.pin.rm(Multihash.fromBase58(cid));
            Assert.assertNotNull(result);
            Assert.assertEquals(1, result.size());
            ipfs.repo.gc();

            Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "/"+ testFileName));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testPublish() {
        // Generate an example file
        String fileContain = "Test publisher!";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFileName));
            writer.write(fileContain);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Publish the file
        cid = Publisher.publish(testFileName, null);

        try {
            // Check the file has been stored in the IPFS node
            assert cid != null;
            byte[] stream = ipfs.cat(Multihash.fromBase58(cid));
            Assert.assertNotNull(stream);
            Assert.assertArrayEquals((fileContain).getBytes(), stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
