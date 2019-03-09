package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.Range.Bound;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
        Debug.userAssert(buf.remaining() == ranges.length, "Buffer should be the same length as the range you're trying to move it into");

        final Range selectedRange = ranges.get(abs(buf.get(0) % ranges.numberOfRanges));

        moveIntoRange(buf, selectedRange);
    }

    public static void moveIntoRange(final ByteBuffer buf, final Range range) {
        Debug.userAssert(buf.remaining() == range.length, "Buffer should be the same length as the range you're trying to move it into");

        boolean considerUpperBound = true;
        boolean considerLowerBound = true;

        for (int byteIdx = 0; byteIdx < range.length; byteIdx++) {
            final int lowerBound =  0xff & (considerLowerBound ? range.get(byteIdx, Bound.LOWER) : 0);
            final int upperBound =  0xff & (considerUpperBound ? range.get(byteIdx, Bound.UPPER) : -1);
            final int rangeAtIdx = (upperBound - lowerBound) + 1;

            Debug.devAssert(Integer.signum(rangeAtIdx) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & buf.get(byteIdx)) % rangeAtIdx) + lowerBound;

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

        Debug.devAssert(() -> range.isIn(Arrays.copyOfRange(buf.array(), buf.arrayOffset(), buf.arrayOffset() + buf.remaining())),
                "Value should have been moved into this range");
    }

    public static void moveIntoRanges(final byte[] arr, final int offset, final Ranges ranges, final int repeat) {
        Debug.userAssert(arr.length - offset == ranges.length * repeat,
                "Buffer should be the same length as the range you're trying to move it into");

        boolean considerUpperBound = true;
        boolean considerLowerBound = true;
        Range range = null;
        int rangeIdx = 0;

        final int todo = ranges.length * repeat;
        for (int byteIdx = offset; byteIdx < todo; byteIdx++) {

            if (rangeIdx == 0) {
                considerLowerBound = true;
                considerUpperBound = true;
                range = ranges.get(abs(arr[byteIdx] % ranges.numberOfRanges));
            }

            final int lowerBound =  0xff & (considerLowerBound ? range.get(rangeIdx, Bound.LOWER) : 0);
            final int upperBound =  0xff & (considerUpperBound ? range.get(rangeIdx, Bound.UPPER) : -1);
            final int rangeAtIdx = (upperBound - lowerBound) + 1;

            Debug.devAssert(Integer.signum(rangeAtIdx) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & arr[byteIdx]) % rangeAtIdx) + lowerBound;

            Debug.devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            Debug.devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            arr[byteIdx]  = (byte) valAtIdxInRange;

            if (considerUpperBound && valAtIdxInRange < upperBound) {
                considerUpperBound = false;
            }

            if (considerLowerBound && lowerBound < valAtIdxInRange) {
                considerLowerBound = false;
            }

            rangeIdx = (rangeIdx + 1) % ranges.length;
        }

        for (int i = 0; i < repeat; i++) {
            Debug.devAssert(() -> ranges.isIn(Arrays.copyOfRange(arr, offset + (ranges.length * repeat), ranges.length)),
                    "Value should have been moved into this range");
        }
    }
}
