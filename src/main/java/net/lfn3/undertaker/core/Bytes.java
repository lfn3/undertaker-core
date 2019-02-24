package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.Range.Bound;

import java.nio.ByteBuffer;

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

        //These are effectively final, only set once during the loop execution below.
        int differentMsbIdx = -1;
        Range selectedRange = null;

        //Only a single range, we can just skip over the first two bits of the loop below.
        if (ranges.numberOfRanges == 1) {
            differentMsbIdx = 0;
            selectedRange = ranges.get(0);
        }

        for (int byteIdx = 0; byteIdx < ranges.length; byteIdx++) {
            if (differentMsbIdx == -1) {
                final byte firstRangeLowerBound = ranges.get(0, byteIdx, Bound.LOWER);
                for (int rangeIdx = 1; rangeIdx < ranges.numberOfRanges; rangeIdx++) {
                    final byte lowerBound = ranges.get(rangeIdx, byteIdx, Bound.LOWER);
                    if (firstRangeLowerBound != lowerBound) {
                        differentMsbIdx = byteIdx;
                        break;
                    }
                }

                if (differentMsbIdx == -1) {
                    //All the ranges are the same up until this point, just set the byte to the range value
                    buf.put(byteIdx, firstRangeLowerBound);
                    continue;
                }
            }

            //At this point differentMsbIdx should contain the index of the point at which the ranges diverge.
            //We pick a range based on the value at this idx.
            //All the earlier bytes in the buffer should have already been moved into the range.
            //TODO: most of the logic in here could use bytes if I go through and sort out the signing.
            //TODO: could possible fuse this with the above loop as well.
            final int valAtIdx = 0xff & buf.get(byteIdx);

            if (selectedRange == null) {
                final int[] msbSkip = new int[ranges.numberOfRanges];

                {
                    final int lowerBound = 0xff & ranges.get(0, byteIdx, Bound.LOWER);
                    final int upperBound = 0xff & ranges.get(0, byteIdx, Bound.UPPER);

                    msbSkip[0] = (upperBound - lowerBound) + 1; //Ranges are inclusive
                }

                for (int rangeIndex = 1; rangeIndex < ranges.numberOfRanges; rangeIndex++) {
                    final int lowerBound = 0xff & ranges.get(rangeIndex, byteIdx, Bound.LOWER);
                    final int upperBound = 0xff & ranges.get(rangeIndex, byteIdx, Bound.UPPER);

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

            Debug.devAssert(selectedRange != null, "Selected range should have been set by this point.");
            @SuppressWarnings("ConstantConditions") // There's a DIY assertion right there.
            final int lowerBound = 0xff & selectedRange.get(byteIdx, Bound.LOWER);
            final int upperBound = 0xff & selectedRange.get(byteIdx, Bound.UPPER);

            final int range = upperBound - lowerBound;

            final int valAtIdxInRange = (valAtIdx % range) + lowerBound;

            Debug.devAssert(lowerBound <= valAtIdxInRange, "Should be gte lower bound");
            Debug.devAssert(valAtIdxInRange <= upperBound, "Should be lte upper bound");

            buf.put(byteIdx, (byte) valAtIdxInRange);
        }

        Debug.devAssert(selectedRange.isIn(buf.array()), "Value should have been moved into this range");
    }

}
