package net.lfn3.undertaker.core;

public class Range {
    public final int length;

    private final int offset;
    private final boolean[] fusedAt;
    protected final byte[] ranges;

    private Range(byte[] ranges, int offset, int length, boolean[] fusedAt) {
        this.length = length;
        this.ranges = ranges;
        this.offset = offset;
        this.fusedAt = fusedAt;

        Debug.devAssert(0 <= offset, "Offset (" + offset + ") should be gte 0");
        Debug.devAssert(0 <= length, "Length (" + length + ") should be gte 0");
        Debug.devAssert(offset + length <= ranges.length, "Ranges array is not long enough.");
        Debug.userAssert(this::isSorted, "Lower bound of range should be less than the upper range.");
        Debug.userAssert(this::doesNotCrossZero, "A single range may not contain both negative and positive values. Please split it.");
    }

    public Range(byte[] range) {
        this(range, 0, range.length / 2, new boolean[range.length / 2]);

        Debug.userAssert(range.length % 2 == 0, "Range length must be a multiple of two, since there's an upper and lower bound.");
    }

    static Range wrap(byte[] ranges, int offset, int length, boolean[] fusedAt) {
        return new Range(ranges, offset, length, fusedAt);
    }

    public byte get(final int byteIndex, final Bound bound) {
        final int unboundIndex = offset + byteIndex;
        Debug.devAssert(0 <= unboundIndex, "Unbounded index (" + unboundIndex + ") should be gte 0");

        if (bound == Bound.UPPER) {
            return ranges[unboundIndex + length];
        } else {
            return ranges[unboundIndex];
        }
    }

    public boolean isIn(byte[] value) {
        //TODO: again, fused ranges are giving me problems here.
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

    private boolean isSorted() {
        for (int byteIdx = 0; byteIdx < length; byteIdx++) {
            final int lowerBound = 0xff & get(byteIdx, Bound.LOWER);
            final int upperBound = 0xff & get(byteIdx, Bound.UPPER);
            if (lowerBound < upperBound) {
                return true;
            } else if (upperBound < lowerBound) {
                return false;
            }
        }

        return true; //Size 1 range
    }

    private boolean doesNotCrossZero() {
        return !(get(0, Bound.LOWER) < 0 && 0 <= get(0, Bound.UPPER));
    }

    public enum Bound {
        UPPER, LOWER
    }
}
