package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.DevDebug;
import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.UserDebug;
import net.lfn3.undertaker.core.intervals.Interval;
import net.lfn3.undertaker.core.intervals.IntervalFlag;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

public class Integers {
    private final ByteSource byteSource;
    private final Intervals intervals;
    private final Booleans booleans;
    private static final Ranges DEFAULT_RANGES;

    static {
        final byte[] range = new byte[Integer.BYTES * 4];
        ByteBuffer wrapRange = ByteBuffer.wrap(range);
        wrapRange.putInt(Integer.MIN_VALUE);
        wrapRange.putInt(-1);
        wrapRange.putInt(0);
        wrapRange.putInt(Integer.MAX_VALUE);

        DEFAULT_RANGES = Ranges.fromFlatArray(range, Integer.BYTES);
    }

    public Integers(ByteSource byteSource, Intervals intervals, Booleans booleans) {
        this.byteSource = byteSource;
        this.intervals = intervals;
        this.booleans = booleans;
    }

    public int next() {
        return next(DEFAULT_RANGES);
    }

    public int next(int max) {
        return next(Integer.MIN_VALUE, max);
    }

    public int next(int min, int max) {
        final byte[] rangeArr;
        final Ranges ranges;
        final int minSig = Integer.signum(min);
        final int maxSig = Integer.signum(max);
        final boolean bothPositive = 0 <= minSig && 0 <= maxSig;
        if (minSig == maxSig || bothPositive) {
            //Don't need to split the range
            rangeArr = new byte[Integer.BYTES * 2];

            final ByteBuffer buf = ByteBuffer.wrap(rangeArr);
            buf.putInt(min);
            buf.putInt(max);

            ranges = Ranges.fromFlatArray(rangeArr, Integer.BYTES);
        } else {
            rangeArr = new byte[Integer.BYTES * 4];

            final ByteBuffer buf = ByteBuffer.wrap(rangeArr);
            buf.putInt(min);
            buf.putInt(-1);
            buf.putInt(0);
            buf.putInt(max);

            ranges = Ranges.fromFlatArray(rangeArr, Integer.BYTES);
        }

        final int retVal = next(ranges);

        DevDebug.devAssert(min <= retVal, "Expected generated value (" + retVal + ")" +
                " to be greater than or equal to supplied min (" + min + ")");
        DevDebug.devAssert(retVal <= max, "Expected generated value (" + retVal + ")" +
                " to be less than or equal to supplied max (" + max + ")");

        return retVal;
    }

    public int next(Ranges ranges) {
        final Interval interval = intervals.next(EnumSet.of(IntervalFlag.VALUE));
        final ByteBuffer buf = byteSource.nextBytes(ranges);
        final int ret = buf.getInt(0);

        intervals.done(interval, ret);
        return ret;
    }

    private boolean shouldGenerateNext()
    {
        return booleans.nextBoolean(5);
    }

    private final static int DEFAULT_MAX_LENGTH = 2048;

    public int[] nextArray() {
        return nextArray(DEFAULT_MAX_LENGTH);
    }

    public int[] nextArray(final int maxLength) {
        return nextArray(0, maxLength);
    }

    public int[] nextArray(final int minLength, final int maxLength) {
        UserDebug.userAssert(minLength <= maxLength,
                "minLength (" + minLength + ") should be less than or equal to maxLength (" + maxLength + ")");

        final int[] tmp = new int[maxLength];

        final Interval collInterval = intervals.next(EnumSet.of(IntervalFlag.COMPOSITE, IntervalFlag.SNIPPABLE_CHILDREN));
        int i = 0;
        for (; i < minLength || (i < maxLength && shouldGenerateNext()); i++) {
            tmp[i] = next();
        }
        final int[] ret = Arrays.copyOf(tmp, i);
        intervals.done(collInterval, ret);
        DevDebug.devAssert(ret.length <= maxLength,
                "Array length (" + ret.length + ") should be less than or equal to supplied max (" + maxLength + ")");
        DevDebug.devAssert(ret.length >= minLength,
                "Array length (" + ret.length + ") should be less than or equal to supplied max (" + maxLength + ")");
        return ret;
    }
}
