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
    public final Intervals intervals = new Intervals();
    public final ByteSource byteSource = new WrappedRandomByteSource();
    public final Booleans booleans = new Booleans(byteSource, intervals);
    public final Longs longs = new Longs(byteSource, intervals, booleans);

    @Benchmark
    public long generateLong()
    {
        return longs.next();
    }

    @Benchmark
    public long[] generateLongArray()
    {
        return longs.nextArray();
    }
}
