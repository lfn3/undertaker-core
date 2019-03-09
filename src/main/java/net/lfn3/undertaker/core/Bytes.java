package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.Range.Bound;

import java.nio.ByteBuffer;

import static java.lang.Math.abs;

public class Bytes {
    /**
     * This attempts to maintain the randomness from the source buffer.
     * We pick a range to move into based on the most significant byte at which the ranges start to diverge.
     *
     * @param buf    input/output buffer, should be prepopulated with random bytes.
     * @param ranges ranges to move the value into
     */
    public static void moveIntoAnyRange(final ByteBuffer buf, final Ranges ranges) {
        Debug.userAssert(buf.capacity() == ranges.length, "Buffer should be the same length as the range you're trying to move it into");

        final Range selectedRange = ranges.get(abs(buf.get(0) % ranges.numberOfRanges));

        boolean considerUpperBound = true;
        boolean considerLowerBound = true;

        for (int byteIdx = 0; byteIdx < selectedRange.length; byteIdx++) {
            final int lowerBound =  0xff & (considerLowerBound ? selectedRange.get( byteIdx, Bound.LOWER) : 0);
            final int upperBound =  0xff & (considerUpperBound ? selectedRange.get( byteIdx, Bound.UPPER) : -1);
            final int range = (upperBound - lowerBound) + 1;

            Debug.devAssert(Integer.signum(range) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & buf.get(byteIdx)) % range) + lowerBound;

            Debug.devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            Debug.devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            buf.put(byteIdx, (byte) valAtIdxInRange);

            if (considerUpperBound && valAtIdxInRange < upperBound) {
                considerUpperBound = false;
            }

            if (considerLowerBound && lowerBound < valAtIdxInRange) {
                considerLowerBound = false;
            }
        }

        Debug.devAssert(() -> selectedRange.isIn(buf.array()), "Value should have been moved into this range");
    }
}
