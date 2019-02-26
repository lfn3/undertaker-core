package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.Debug;
import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.intervals.Interval;
import net.lfn3.undertaker.core.intervals.IntervalFlag;
import net.lfn3.undertaker.core.intervals.IntervalType;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

public class Longs {
    private final ByteSource byteSource;
    private final Intervals intervals;
    private final Booleans booleans;

    public Longs(ByteSource byteSource, Intervals intervals, Booleans booleans) {
        this.byteSource = byteSource;
        this.intervals = intervals;
        this.booleans = booleans;
    }

    public long next() {
        final byte[] range = new byte[Long.BYTES * 4];
        Arrays.fill(range, 0, Long.BYTES, Byte.MIN_VALUE);
        Arrays.fill(range, Long.BYTES, Long.BYTES * 2, (byte) -1);
        Arrays.fill(range, Long.BYTES * 2, Long.BYTES * 3, (byte) 0);
        Arrays.fill(range, Long.BYTES * 3, Long.BYTES * 4, Byte.MAX_VALUE);

        final Ranges ranges = Ranges.fromFlatArray(range, Long.BYTES);

        return next(ranges);
    }

    public long next(long max) {
        return next(Long.MIN_VALUE, max);
    }

    public long next(long min, long max) {
        final byte[] rangeArr;
        final Ranges ranges;
        final int minSig = Long.signum(min);
        final int maxSig = Long.signum(max);
        final boolean bothPositive = minSig <= 0 && maxSig <= 0;
        if (minSig == maxSig || bothPositive) {
            //Don't need to split the range
            rangeArr = new byte[Long.BYTES * 2];

            final ByteBuffer buf = ByteBuffer.wrap(rangeArr);
            buf.putLong(min);
            buf.putLong(max);

            ranges = Ranges.fromFlatArray(rangeArr, Long.BYTES);
        } else {
            rangeArr = new byte[Long.BYTES * 4];

            final ByteBuffer buf = ByteBuffer.wrap(rangeArr);
            buf.putLong(min);
            buf.putLong(-1);
            buf.putLong(0);
            buf.putLong(max);

            ranges = Ranges.fromFlatArray(rangeArr, Long.BYTES);
        }

        final long retVal = next(ranges);

        Debug.devAssert(min <= retVal, "Expected generated value (" + retVal + ")" +
                " to be greater than or equal to supplied min (" + min + ")");
        Debug.devAssert(retVal <= max, "Expected generated value (" + retVal + ")" +
                " to be less than or equal to supplied max (" + max + ")");

        return retVal;
    }

    public long next(Ranges ranges) {
        final Interval interval = intervals.next(IntervalType.VALUE);
        final ByteBuffer buf = byteSource.nextBytes(ranges);
        final long ret = buf.getLong();

        intervals.done(interval, ret);
        return ret;
    }

    private boolean shouldGenerateNext()
    {
        return booleans.nextBoolean(5);
    }

    private final static int DEFAULT_MAX_LENGTH = 2048;

    public long[] nextArray() {
        return nextArray(DEFAULT_MAX_LENGTH);
    }

    public long[] nextArray(final int maxLength) {
        final long[] tmp = new long[maxLength];

        final Interval collInterval = intervals.next(IntervalType.COMPOSITE, EnumSet.of(IntervalFlag.SNIPPABLE_CHILDREN));
        int i = 0;
        for (;i < maxLength && shouldGenerateNext(); i++) {
            tmp[i] = next();
        }
        final long[] ret = Arrays.copyOf(tmp, i);
        intervals.done(collInterval, ret);
        Debug.devAssert(ret.length <= maxLength,
                "Array length (" + ret.length + ") should be less than or equal to supplied max (" + maxLength + ")");
        return ret;
    }
}
