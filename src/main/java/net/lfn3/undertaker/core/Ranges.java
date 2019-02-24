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

    private Ranges(final byte[] ranges, final int length)
    {
        this.ranges = ranges;
        this.length = length;
        this.numberOfRanges = ranges.length / pairLength();

        Debug.userAssert(length > 0, "Length must greater than zero");
        Debug.userAssert(ranges.length > 0, "Ranges must contain some values");
        Debug.userAssert( ranges.length % pairLength() == 0, "Expected ranges to contain 2x length bytes, or a multiple of that");
        Debug.userAssert(rangesAreInCorrectDirection(ranges, length), "");
        Debug.userAssert(areRangesSorted(ranges, length), "");
        Debug.userAssert(rangesDoNotOverlap(ranges, length), "");
    }

    static boolean rangesAreInCorrectDirection(byte[] ranges, int length) {
        //TODO
        return true;
    }

    static boolean areRangesSorted(final byte[] ranges, int length)
    {
        //TODO
        return true;
    }

    static boolean rangesDoNotOverlap(final byte[] ranges, int length)
    {
        //TODO
        return true;
    }

    public static Ranges fromFlatArray(final byte[] ranges, final int length)
    {
        return new Ranges(ranges, length);
    }

    private int pairLength()
    {
        return length * 2;
    }

    public byte get(final int rangeIndex, final int byteIndex, Bound bound)
    {
        Debug.userAssert(
                rangeIndex < numberOfRanges,
                "Range index (" + rangeIndex + ") must be less than the number of ranges (" + numberOfRanges + ")." );
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

    public Range get(final int rangeIndex)
    {
        Debug.userAssert(
                rangeIndex < numberOfRanges,
                "Range index (" + rangeIndex + ") must be less than the number of ranges (" + numberOfRanges + ")." );

        final int offset = rangeIndex * pairLength();

        return Range.fromRanges(ranges, offset, length);
    }
}
