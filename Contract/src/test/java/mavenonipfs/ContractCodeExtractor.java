package mavenonipfs;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.tooling.deploy.OptimizedJarBuilder;
import org.junit.Test;

/**
 * This test includes methods that are used to build resources
 */
public class ContractCodeExtractor {

    private Class[] otherClasses = {MavenOnIPFSEvents.class};

    /**
     * Used to build the contract jar file
     */
    @Test
    public void buildJar() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(MavenOnIPFS.class, otherClasses);
        byte[] optimizedJar = (new OptimizedJarBuilder(false, jar, 1))
            .withUnreachableMethodRemover()
            .withRenamer()
            .withConstantRemover()
            .getOptimizedBytes();

        try {
            DataOutputStream dout = new DataOutputStream(new FileOutputStream("mavenonipfs.jar"));
            dout.write(optimizedJar);
            dout.close();
        } catch (IOException e) {
            System.err.println("Failed to create the jar.");
            e.printStackTrace();
        }
    }
}
