package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.UserDebug;
import net.lfn3.undertaker.core.intervals.Interval;
import net.lfn3.undertaker.core.intervals.IntervalFlag;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;

import java.nio.ByteBuffer;
import java.util.EnumSet;

public class Booleans {
    private final ByteSource byteSource;
    private final Intervals intervals;

    public Booleans(ByteSource byteSource, Intervals intervals) {
        this.byteSource = byteSource;
        this.intervals = intervals;
    }

    public boolean nextBoolean() {
        final Range range = new Range(new byte[]{0, 1});
        return nextBoolean(range);
    }

    public boolean nextBoolean(final byte bias) {
        UserDebug.userAssert(bias >= 1, "Bias should be greater than or equal to one.");
        final Range range = new Range(new byte[]{0, bias});
        return nextBoolean(range);
    }

    public boolean nextBoolean(final int bias) {
        UserDebug.userAssert(bias <= Byte.MAX_VALUE, "Bias must be less than 128");
        return nextBoolean((byte)bias);
    }

    private boolean nextBoolean(final Range range) {
        final Interval interval = intervals.next(EnumSet.of(IntervalFlag.VALUE));
        final ByteBuffer buf = byteSource.nextBytes(range);
        final boolean ret = buf.get(0) >= 1;
        intervals.done(interval, ret);
        return ret;
    }
}
