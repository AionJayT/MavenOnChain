package cli;

import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import net.i2p.crypto.eddsa.Utils;

/**
 * Creates an ABI-encoded call payload when given a method name and 0 or more parameters.
 */
public class ComposeCallPayload {
    public static void main(String[] args) {
        if (0 == args.length) {
            System.err.println("Usage: cli.ComposeCallPayload METHOD_NAME [PARAMETER]*");
            System.exit(1);
        }

        byte[] rawCallData = null;
        String methodName = args[0];
        switch (methodName) {
            case "claimGroupId":
            case "deClaimGroupId":
                if (args.length != 2) {
                    System.err.println("getRegisterGroupIdPayload error!, input arguments size");
                    System.exit(1);
                }
                rawCallData = getRegisterGroupIdPayload(args);
                break;
            case "publish":
                if (args.length != 6) {
                    System.err.println("getPublishPayload error!, invalid input arguments size");
                    System.exit(1);
                }
                rawCallData = getPublishPayload(args);
                break;
            default:
                System.err.println("Method " + methodName + " is not defined.");
                System.exit(1);
        }
        System.out.println("0x" + Utils.bytesToHex(rawCallData));
    }

    private static byte[] getRegisterGroupIdPayload(String[] args) {
        String methodName = args[0];
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        encoder.encodeOneString(methodName);
        encoder.encodeOneString(args[1]);
        return encoder.toBytes();
    }

    private static byte[] getPublishPayload(String[] args) {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        encoder.encodeOneString(args[0]);   // method call
        encoder.encodeOneString(args[1]);   // groupId
        encoder.encodeOneString(args[2]);   // artifactId
        encoder.encodeOneString(args[3]);   // version
        encoder.encodeOneByte(Byte.valueOf(args[4])); // type

        String arg5 = args[5].startsWith("0x") ? args[5].substring(2): args[5];
        encoder.encodeOneByteArray(Utils.hexToBytes(arg5));   // multiHash
        return encoder.toBytes();
    }
}
