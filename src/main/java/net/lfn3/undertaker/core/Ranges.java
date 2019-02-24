package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.Range.Bound;

/**
 * Ranges are inclusive, and used to tell sources what a valid range of output is.
 */
public class Ranges {
    //TODO should this all be copied into big contiguous arrays?
    // Might be able to do some tricks involving flyweights and pooling if so.

    public final int length;
    public final int numberOfRanges;

    private final byte[] ranges;

    private Ranges(final byte[] ranges, final int length) {
        this.ranges = ranges;
        this.length = length;
        this.numberOfRanges = ranges.length / pairLength();

        Debug.userAssert(length > 0, "Length must greater than zero");
        Debug.userAssert(ranges.length > 0, "Ranges must contain some values");
        Debug.userAssert(ranges.length % pairLength() == 0, "Expected ranges to contain 2x length bytes, or a multiple of that");
        Debug.userAssert(areRangesSorted(), "");
        Debug.userAssert(rangesDoNotOverlap(ranges, length), "");
        Debug.userAssert(rangesDoNotCrossZero(ranges, length), "");
    }

    boolean areRangesSorted() {
        for (int rangeIdx = 1; rangeIdx < numberOfRanges; rangeIdx++) {
            boolean lastUpperIsLower = false;
            for (int byteIndex = 0; byteIndex < length; byteIndex++)
            {
                final byte lastUpper = get(rangeIdx - 1, byteIndex, Bound.UPPER);
                final byte currentLower = get(rangeIdx, byteIndex, Bound.LOWER);

                if (lastUpper < currentLower) {
                    lastUpperIsLower = true;
                    break;
                } else if (currentLower < lastUpper) {
                    return false;
                }
            }

            if (!lastUpperIsLower) {
                return false;
            }
        }
        return true;
    }

    static boolean rangesDoNotOverlap(final byte[] ranges, int length) {
        //TODO
        return true;
    }

    static boolean rangesDoNotCrossZero(final byte[] ranges, int length) {
        //TODO
        return true;
    }

    public static Ranges fromFlatArray(final byte[] ranges, final int length) {
        return new Ranges(ranges, length);
    }

    public static Ranges fromArrays(final byte[] lowerBound, final byte[] upperBound, final byte[]... moreRanges) {
        Debug.userAssert(lowerBound.length == upperBound.length, "All inputs must be of same length");
        final byte[] flattened = new byte[lowerBound.length + upperBound.length + (moreRanges.length * lowerBound.length)];
        int pos = 0;
        System.arraycopy(lowerBound, 0, flattened, pos, lowerBound.length);
        pos += lowerBound.length;
        System.arraycopy(upperBound, 0, flattened, pos, upperBound.length);
        pos += upperBound.length;
        for (byte[] arr : moreRanges) {
            Debug.userAssert(arr.length == lowerBound.length, "All inputs must be of same length");
            System.arraycopy(arr, 0, flattened, pos, arr.length);
            pos += arr.length;
        }
        return Ranges.fromFlatArray(flattened, lowerBound.length);
    }

    private int pairLength() {
        return length * 2;
    }

    public byte get(final int rangeIndex, final int byteIndex, Bound bound) {
        Debug.userAssert(
                rangeIndex < numberOfRanges,
                "Range index (" + rangeIndex + ") must be less than the number of ranges (" + numberOfRanges + ").");
        Debug.userAssert(
                byteIndex < length,
                "Byte index (" + byteIndex + ") must be less than the length of the ranges (" + length + ").");

        int unboundedIdx = rangeIndex * pairLength() + byteIndex;

        if (bound == Bound.UPPER) {
            return ranges[unboundedIdx + length];
        } else {
            return ranges[unboundedIdx];
        }
    }

    public Range get(final int rangeIndex) {
        Debug.userAssert(
                rangeIndex < numberOfRanges,
                "Range index (" + rangeIndex + ") must be less than the number of ranges (" + numberOfRanges + ").");

        final int offset = rangeIndex * pairLength();

        return Range.wrap(ranges, offset, length);
    }

    public boolean isIn(final byte[] value) {
        if (value.length > length) {
            return false;
        }

        Range r;
        for (int rangeIdx = 0; rangeIdx < numberOfRanges; rangeIdx++) {
            r = get(rangeIdx);
            if (r.isIn(value)) {
                return true;
            }
        }

        return false;
    }
}
