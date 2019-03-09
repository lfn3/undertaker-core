package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.generators.Booleans;
import net.lfn3.undertaker.core.generators.Longs;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;
import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class LongsBench {
    public static final int ARR_SIZE = 1024;

    public final Intervals intervals = new Intervals();
    public final ByteSource byteSource = new WrappedRandomByteSource();
    public final Booleans booleans = new Booleans(byteSource, intervals);
    public final Longs longs = new Longs(byteSource, intervals);
    public final long[] arrayToFill = new long[ARR_SIZE];

    @Benchmark
    public long generateLong()
    {
        return longs.next();
    }

    @Benchmark
    public long[] generateLongArray()
    {
        return longs.nextArray(ARR_SIZE, ARR_SIZE);
    }

    @Benchmark
    public long[] fillLongArray() {
        longs.fill(arrayToFill);
        return arrayToFill;
    }
}
