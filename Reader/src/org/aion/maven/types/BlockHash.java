package org.aion.maven.types;

import java.util.Arrays;


/**
 * A 32-byte Aion block hash.
 */
public final class BlockHash {
    private final static int BLOCK_HASH_BYTES = 32;
    private final byte[] bytes;

    public BlockHash(byte[] bytes) {
        if (BLOCK_HASH_BYTES != bytes.length) {
            throw new IllegalArgumentException();
        }
        this.bytes = bytes.clone();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (obj == this);
        if (!isEqual && (null != obj) && (obj.getClass() == getClass())) {
            isEqual = Arrays.equals(this.bytes, ((BlockHash)obj).bytes);
        }
        return isEqual;
    }
}
