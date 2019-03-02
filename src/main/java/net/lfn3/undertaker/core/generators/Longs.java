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
    private static final Ranges DEFAULT_RANGES;

    static {
        final byte[] range = new byte[Long.BYTES * 4];
        Arrays.fill(range, 0, Long.BYTES, Byte.MIN_VALUE);
        Arrays.fill(range, Long.BYTES, Long.BYTES * 2, (byte) -1);
        Arrays.fill(range, Long.BYTES * 2, Long.BYTES * 3, (byte) 0);
        Arrays.fill(range, Long.BYTES * 3, Long.BYTES * 4, Byte.MAX_VALUE);

        DEFAULT_RANGES = Ranges.fromFlatArray(range, Long.BYTES);
    }

    public Longs(ByteSource byteSource, Intervals intervals, Booleans booleans) {
        this.byteSource = byteSource;
        this.intervals = intervals;
        this.booleans = booleans;
    }

    public long next() {
        return next(DEFAULT_RANGES);
    }

    public long next(long max) {
        return next(Long.MIN_VALUE, max);
    }

    public long next(long min, long max) {
        final byte[] rangeArr;
        final Ranges ranges;
        final int minSig = Long.signum(min);
        final int maxSig = Long.signum(max);
        final boolean bothPositive =  0 <= minSig  && 0 <= maxSig;
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
        final long ret = buf.getLong(0);

        intervals.done(interval, ret);
        return ret;
    }

    private boolean shouldGenerateNext()
    {
        return booleans.nextBoolean(5);
    }

    public final static int DEFAULT_MAX_LENGTH = 2048;

    public long[] nextArray() {
        return nextArray(DEFAULT_MAX_LENGTH);
    }

    public long[] nextArray(final int maxLength) {
        return nextArray(0, maxLength);
    }

    public long[] nextArray(final int minLength, final int maxLength) {
        Debug.userAssert(minLength <= maxLength,
                "minLength (" + minLength + ") should be less than or equal to maxLength (" + maxLength + ")");

        final long[] tmp = new long[maxLength];

        final Interval collInterval = intervals.next(IntervalType.COMPOSITE, EnumSet.of(IntervalFlag.SNIPPABLE_CHILDREN));
        int i = 0;
        for (; i < minLength || (i < maxLength && shouldGenerateNext()); i++) {
            tmp[i] = next();
        }
        final long[] ret = Arrays.copyOf(tmp, i);
        intervals.done(collInterval, ret);
        Debug.devAssert(ret.length <= maxLength,
                "Array length (" + ret.length + ") should be less than or equal to supplied max (" + maxLength + ")");
        Debug.devAssert(ret.length >= minLength,
                "Array length (" + ret.length + ") should be less than or equal to supplied max (" + maxLength + ")");
        return ret;
    }

    /**
     * You should only use this if the code you're testing is designed to avoid allocation.
     * It will not shrink as well as the {@link #nextArray()} methods since it will not reduce the size of the array.
     * @param toFill array to fill with random longs
     */
    public void fill(final long[] toFill) {
        for (int i = 0; i < toFill.length; i++) {
            toFill[i] = next();
        }
    }
}
