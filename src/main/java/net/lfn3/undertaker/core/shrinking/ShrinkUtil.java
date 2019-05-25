package net.lfn3.undertaker.core.shrinking;

public class ShrinkUtil {
    static int firstNonZeroByteIndex(byte[] bytes) {
        return firstNonZeroByteIndex(bytes, 0);
    }

    static int firstNonZeroByteIndex(byte[] bytes, int startFrom) {
        for (int i = startFrom; i < bytes.length; i++) {
            if (bytes[i] != 0) {
                return i;
            }
        }

        return -1;
    }
}
