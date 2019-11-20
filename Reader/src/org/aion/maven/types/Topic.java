package org.aion.maven.types;

import java.util.Arrays;

import org.aion.maven.blockchain.Codecs;


/**
 * A 32-byte event log topic.
 */
public final class Topic {
    private final static int TOPIC_BYTES = 32;

    /**
     * Creates a new Topic instance from already 32-byte data.
     * 
     * @param bytes The bytes making up the topic.
     */
    public static Topic createFromPreciseData(byte[] bytes) {
        return new Topic(bytes);
    }

    /**
     * Creates a new Topic instance, right-padding or truncating to 32-bytes, if required.
     * 
     * @param bytes The bytes making up the topic (will be right-padded with 0x0 or truncated to fit 32 bytes).
     */
    public static Topic createFromArbitraryData(byte[] bytes) {
        byte[] preciseBytes = (TOPIC_BYTES == bytes.length)
                ? bytes
                : Arrays.copyOf(bytes, TOPIC_BYTES);
        return new Topic(preciseBytes);
    }


    private final byte[] bytes;

    private Topic(byte[] bytes) {
        if (TOPIC_BYTES != bytes.length) {
            throw new IllegalArgumentException();
        }
        this.bytes = bytes.clone();
    }

    /**
     * @return This returns the value as a complete number, meaning the 0x hex prefix is included.
     */
    public String renderedAsHexNumber() {
        return "0x" + Codecs.bytesToHexString(this.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (obj == this);
        if (!isEqual && (null != obj) && (obj.getClass() == getClass())) {
            Topic other = (Topic) obj;
            isEqual = Arrays.equals(this.bytes, other.bytes);
        }
        return isEqual;
    }
}
