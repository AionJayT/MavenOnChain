package org.aion.maven.types;

import java.util.Arrays;


/**
 * A 32-byte Aion address.
 */
public final class Address {
    private final static int ADDRESS_BYTES = 32;
    private final byte[] bytes;

    public Address(byte[] bytes) {
        if (ADDRESS_BYTES != bytes.length) {
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
            isEqual = Arrays.equals(this.bytes, ((Address)obj).bytes);
        }
        return isEqual;
    }
}
