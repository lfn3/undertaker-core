package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.Range.Bound;

import java.nio.ByteBuffer;

import static java.lang.Math.abs;
import static net.lfn3.undertaker.core.DevDebug.devAssert;
import static net.lfn3.undertaker.core.UserDebug.userAssert;

public class Bytes {
    /**
     * @param buf    input/output buffer, should be prepopulated with random bytes.
     * @param ranges ranges to move the value into
     */
    public static void moveIntoRange(final ByteBuffer buf, final Ranges ranges) {
        userAssert(buf.remaining() % ranges.length == 0,
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

            final int lowerBound = 0xff & (considerLowerBound ? range.get(rangeIdx, Bound.LOWER) : 0);
            final int upperBound = 0xff & (considerUpperBound ? range.get(rangeIdx, Bound.UPPER) : -1);
            final int rangeAtIdx = (upperBound - lowerBound) + 1;

            devAssert(Integer.signum(rangeAtIdx) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & valAtIdx) % rangeAtIdx) + lowerBound;

            devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            buf.put(byteIdx, (byte) valAtIdxInRange);

            if (considerUpperBound && valAtIdxInRange < upperBound) {
                considerUpperBound = false;
            }

            if (considerLowerBound && lowerBound < valAtIdxInRange) {
                considerLowerBound = false;
            }
        }

        buf.rewind();

        devAssert(() -> bufInRanges(buf, ranges), "Values should have been moved into this range");
    }

    public static void moveIntoRange(final ByteBuffer buf, final Range range) {
        userAssert(buf.remaining() % range.length == 0,
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

            final int lowerBound = 0xff & (considerLowerBound ? range.get(rangeIdx, Bound.LOWER) : 0);
            final int upperBound = 0xff & (considerUpperBound ? range.get(rangeIdx, Bound.UPPER) : -1);
            final int rangeAtIdx = (upperBound - lowerBound) + 1;

            devAssert(Integer.signum(rangeAtIdx) > 0, "Ranges must contain at least a single value");

            final int valAtIdxInRange = ((0xff & valAtIdx) % rangeAtIdx) + lowerBound;

            devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            buf.put(byteIdx, (byte) valAtIdxInRange);

            if (considerUpperBound && valAtIdxInRange < upperBound) {
                considerUpperBound = false;
            }

            if (considerLowerBound && lowerBound < valAtIdxInRange) {
                considerLowerBound = false;
            }
        }

        buf.rewind();

        devAssert(() -> bufInRanges(buf, Ranges.fromRange(range)),
                "Values should have been moved into this range");
    }

    /**
     * This attempts to maintain the randomness from the source buffer.
     * We pick a range to move into based on the most significant byte at which the ranges start to diverge.
     *
     * @param buf    input/output buffer, should be prepopulated with random bytes.
     * @param ranges ranges to move the value into
     */
    public static void moveIntoNearestRange(final ByteBuffer buf, final Ranges ranges) {
        userAssert(buf.remaining() % ranges.length == 0,
                "Buffer remaining should be a multiple of the length of the range you're trying to move it into");

        for (int application = 0; application < buf.remaining() / ranges.length; application++) {

            //These are effectively final, only set once during the loop execution below.
            int differentMsbIdx = -1;
            Range selectedRange = null;

            //Only a single range, we can just skip over the first two bits of the loop below.
            if (ranges.numberOfRanges == 1) {
                differentMsbIdx = 0;
                selectedRange = ranges.get(0);
            }

            boolean considerUpperBound = true;
            boolean considerLowerBound = true;

            for (int rangeByteIdx = 0; rangeByteIdx < ranges.length; rangeByteIdx++) {
                int bufByteIdx = rangeByteIdx + application * ranges.length;
                if (differentMsbIdx == -1) {
                    final byte firstRangeLowerBound = ranges.get(0, rangeByteIdx, Bound.LOWER);
                    for (int rangeIdx = 1; rangeIdx < ranges.numberOfRanges; rangeIdx++) {
                        final byte lowerBound = ranges.get(rangeIdx, rangeByteIdx, Bound.LOWER);
                        if (firstRangeLowerBound != lowerBound) {
                            differentMsbIdx = rangeByteIdx;
                            break;
                        }
                    }

                    if (differentMsbIdx == -1) {
                        //All the ranges are the same up until this point, just set the byte to the range value
                        buf.put(bufByteIdx, firstRangeLowerBound);
                        continue;
                    }
                }

                devAssert(differentMsbIdx != -1, "MSB index should have been set by now.");

                //At this point differentMsbIdx should contain the index of the point at which the ranges diverge.
                //We pick a range based on the value at this idx.
                //All the earlier bytes in the buffer should have already been moved into the range.
                //TODO: most of the logic in here could use bytes if I go through and sort out the signing.
                //TODO: could possible fuse this with the above loop as well.
                final int valAtIdx = 0xff & buf.get(bufByteIdx);

                if (selectedRange == null) {
                    final int[] msbSkip = new int[ranges.numberOfRanges];

                    {
                        final int lowerBound = 0xff & ranges.get(0, rangeByteIdx, Bound.LOWER);
                        final int upperBound = 0xff & ranges.get(0, rangeByteIdx, Bound.UPPER);

                        msbSkip[0] = (upperBound - lowerBound) + 1; //Ranges are inclusive
                    }

                    for (int rangeIndex = 1; rangeIndex < ranges.numberOfRanges; rangeIndex++) {
                        final int lowerBound = 0xff & ranges.get(rangeIndex, rangeByteIdx, Bound.LOWER);
                        final int upperBound = 0xff & ranges.get(rangeIndex, rangeByteIdx, Bound.UPPER);

                        msbSkip[rangeIndex] = msbSkip[rangeIndex - 1] + (upperBound - lowerBound);
                    }

                    int withinRange = valAtIdx % msbSkip[msbSkip.length - 1];

                    for (int i = 0; i < msbSkip.length; i++) {
                        if (withinRange <= msbSkip[i]) {
                            selectedRange = ranges.get(i);
                        }
                    }
                }

                // Actually move it into the selected range (finally!)

                devAssert(selectedRange != null, "Selected range should have been set by this point.");
                final int lowerBound = 0xff & (considerLowerBound ? selectedRange.get(rangeByteIdx, Bound.LOWER) : 0);
                final int upperBound = 0xff & (considerUpperBound ? selectedRange.get(rangeByteIdx, Bound.UPPER) : -1);
                final int range = (upperBound - lowerBound) + 1;

                devAssert(Integer.signum(range) > 0, "Ranges must contain at least a single value");

                final int valAtIdxInRange = (valAtIdx % range) + lowerBound;

                devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
                devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

                buf.put(bufByteIdx, (byte) valAtIdxInRange);

                if (considerUpperBound && valAtIdxInRange < upperBound) {
                    considerUpperBound = false;
                }

                if (considerLowerBound && lowerBound < valAtIdxInRange) {
                    considerLowerBound = false;
                }
            }
            devAssert(selectedRange != null, "Should have got a selected range at this point");
        }

        devAssert(() -> bufInRanges(buf, ranges), "Values should have been moved into this range");
    }

    private static Boolean bufInRanges(ByteBuffer buf, Ranges ranges) {
        final byte[] slicedValue = new byte[ranges.length];
        while (buf.hasRemaining()) {
            buf.get(slicedValue);
            if (!ranges.isIn(slicedValue)) {
                return false;
            }
        }

        buf.rewind();
        return true;
    }
}
