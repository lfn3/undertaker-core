package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.DevDebug;
import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.intervals.Interval;
import net.lfn3.undertaker.core.intervals.IntervalType;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
public class Longs {
    private static final Ranges DEFAULT_RANGES;

    private final ByteSource byteSource;
    private final Intervals intervals;
    private final Collections collections;

    static {
        {
            final byte[] range = new byte[Long.BYTES * 4];
            ByteBuffer wrapRange = ByteBuffer.wrap(range);
            wrapRange.putLong(Long.MIN_VALUE);
            wrapRange.putLong(-1);
            wrapRange.putLong(0);
            wrapRange.putLong(Long.MAX_VALUE);

            DEFAULT_RANGES = Ranges.fromFlatArray(range, Long.BYTES);
        }
    }

    public Longs(ByteSource byteSource, Intervals intervals, Integers integers) {
        this.byteSource = byteSource;
        this.intervals = intervals;
        this.collections = new Collections(byteSource, intervals, integers);
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

        DevDebug.devAssert(min <= retVal, "Expected generated value (" + retVal + ")" +
                " to be greater than or equal to supplied min (" + min + ")");
        DevDebug.devAssert(retVal <= max, "Expected generated value (" + retVal + ")" +
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

    public final static int DEFAULT_MAX_LENGTH = 2048;

    public long[] nextArray() {
        return nextArray(DEFAULT_MAX_LENGTH);
    }

    public long[] nextArray(final int maxLength) {
        return nextArray(0, maxLength);
    }

    public long[] nextArray(final int minLength, final int maxLength) {
        final Interval collInterval = intervals.next(IntervalType.COLLECTION);
        final int size = collections.getSize(minLength, maxLength);
        final long[] out = new long[size];

        fillArray(out);

        intervals.done(collInterval, out);

        return out;
    }

    private void fillArray(final long[] toFill) {
        byteSource.pregen(toFill.length * Long.BYTES);
        final LongBuffer longBuffer = byteSource.nextBytes(DEFAULT_RANGES, toFill.length)
                .order(ByteOrder.BIG_ENDIAN)
                .asLongBuffer();
        longBuffer.get(toFill);
    }

    /**
     * You should only use this if the code you're testing is designed to avoid allocation.
     * It will not shrink as well as the {@link #nextArray()} methods since it will not reduce the size of the array.
     * @param toFill array to fill with random longs
     */
    public void fill(final long[] toFill) {
        final Interval collInterval = intervals.next(IntervalType.COLLECTION);
        fillArray(toFill);
        intervals.done(collInterval, toFill);
    }
}
