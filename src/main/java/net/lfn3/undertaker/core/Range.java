package net.lfn3.undertaker.core;

public class Range {
    final int length;

    private final int offset;
    private final byte[] ranges;

    private Range(byte[] ranges, int offset, int length) {
        this.length = length;
        this.ranges = ranges;
        this.offset = offset;
    }

    public static Range fromRanges(byte[] ranges, int offset, int length) {
        return new Range(ranges, offset, length);
    }

    public byte get(final int byteIndex, final Bound bound) {
        final int unboundIndex = offset + byteIndex;

        if (bound == Bound.UPPER) {
            return ranges[unboundIndex + length];
        } else {
            return ranges[unboundIndex];
        }
    }

    public enum Bound {
        UPPER, LOWER
    }
}
