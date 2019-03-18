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
    public static void moveIntoRange(final ByteBuffer buf, final Ranges ranges) {
        Debug.userAssert(buf.remaining() % ranges.length == 0,
                "Buffer remaining should be a multiple of the length of the range you're trying to move it into");

        boolean considerUpperBound = true;
        boolean considerLowerBound = true;
        Range range = null;

        for (int byteIdx = 0; byteIdx < buf.limit(); byteIdx++) {
            final byte valAtIdx = buf.get(byteIdx);
            final int rangeIdx = byteIdx % ranges.length;
            if (rangeIdx == 0) {
                considerLowerBound = true;
                considerUpperBound = true;
                range = ranges.get(abs(valAtIdx % ranges.numberOfRanges));
            }

            final int lowerBound =  0xff & (considerLowerBound ? range.get(rangeIdx, Bound.LOWER) : 0);
            final int upperBound =  0xff & (considerUpperBound ? range.get(rangeIdx, Bound.UPPER) : -1);
            final int rangeAtIdx = (upperBound - lowerBound) + 1;

            Debug.devAssert(Integer.signum(rangeAtIdx) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & valAtIdx) % rangeAtIdx) + lowerBound;

            Debug.devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            Debug.devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            buf.put(byteIdx, (byte)valAtIdxInRange);

            if (considerUpperBound && valAtIdxInRange < upperBound) {
                considerUpperBound = false;
            }

            if (considerLowerBound && lowerBound < valAtIdxInRange) {
                considerLowerBound = false;
            }
        }

        buf.rewind();

        Debug.devAssert(() -> {
                    final byte[] slicedValue = new byte[ranges.length];
                    while (buf.hasRemaining()) {
                        buf.get(slicedValue);
                        if (!ranges.isIn(slicedValue)) {
                            return false;
                        }
                    }

                    buf.rewind();
                    return true;
                },
                "Values should have been moved into this range");
    }

    public static void moveIntoRange(final ByteBuffer buf, final Range range) {
        Debug.userAssert(buf.remaining() % range.length == 0,
                "Buffer remaining should be a multiple of the length of the range you're trying to move it into");

        boolean considerUpperBound = true;
        boolean considerLowerBound = true;

        for (int byteIdx = 0; byteIdx < buf.limit(); byteIdx++) {
            final int rangeIdx = byteIdx % range.length;
            final byte valAtIdx = buf.get(byteIdx);
            if (rangeIdx == 0) {
                considerLowerBound = true;
                considerUpperBound = true;
            }

            final int lowerBound =  0xff & (considerLowerBound ? range.get(rangeIdx, Bound.LOWER) : 0);
            final int upperBound =  0xff & (considerUpperBound ? range.get(rangeIdx, Bound.UPPER) : -1);
            final int rangeAtIdx = (upperBound - lowerBound) + 1;

            Debug.devAssert(Integer.signum(rangeAtIdx) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & valAtIdx) % rangeAtIdx) + lowerBound;

            Debug.devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            Debug.devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            buf.put(byteIdx, (byte)valAtIdxInRange);

            if (considerUpperBound && valAtIdxInRange < upperBound) {
                considerUpperBound = false;
            }

            if (considerLowerBound && lowerBound < valAtIdxInRange) {
                considerLowerBound = false;
            }
        }

        buf.rewind();

        Debug.devAssert(() -> {
                    final byte[] slicedValue = new byte[range.length];
                    while (buf.hasRemaining()) {
                        buf.get(slicedValue);
                        if (!range.isIn(slicedValue)) {
                            return false;
                        }
                    }

                    buf.rewind();
                    return true;
                },
                "Values should have been moved into this range");    }
}
