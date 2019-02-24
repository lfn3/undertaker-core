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

    static Range wrap(byte[] ranges, int offset, int length) {
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

    public boolean isIn(byte[] value) {
        if (value.length > length) {
            return false;
        }

        boolean considerUpperBound = true;
        boolean considerLowerBound = true;

        for (int byteIdx = 0; byteIdx < value.length; byteIdx++) {
            if (!considerLowerBound && !considerUpperBound) {
                //We've bounced off both edges of the range
                return true;
            }

            final int atIdx = 0xff & value[byteIdx];

            final int lowerBound = considerLowerBound ?
                    0xff & get(byteIdx, Bound.LOWER) :
                    0;
            final int upperBound = considerUpperBound ?
                    0xff & get(byteIdx, Bound.UPPER) :
                    255;

            if (lowerBound < atIdx && atIdx < upperBound) {
                //If it's in the middle of a range, doesn't matter what the following values are.
                return true;
            } else if (considerUpperBound && lowerBound == atIdx && upperBound != lowerBound) {
                //If we're on the lower bound, we don't need to worry about the upper bound anymore.
                considerUpperBound = false;
                continue;
            } else if (considerLowerBound && upperBound == atIdx && upperBound != lowerBound) {
                //If we're on the upper bound, we don't need to worry about the lower bound anymore.
                considerLowerBound = false;
                continue;
            } else if (lowerBound <= atIdx && atIdx <= upperBound) {
                continue;
            }

            return false;
        }

        return true;
    }

    public enum Bound {
        UPPER, LOWER
    }
}
