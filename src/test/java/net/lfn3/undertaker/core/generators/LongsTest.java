package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;
import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.junit.Test;

public class LongsTest {
    private static final int ITERATIONS = 10000;

    private final Intervals intervals = new Intervals(1);
    private final ByteSource randomSource = new WrappedRandomByteSource();
    private final Longs defaultGen = new Longs(randomSource, intervals);

    @Test
    public void canGenerateLongs() {
        defaultGen.nextLong();
    }

    @Test
    public void canGenerateLongsInRange() {
        for (int i = 0; i < ITERATIONS; i++) {
            final long max = defaultGen.nextLong(Long.MAX_VALUE - 1);
            final long min = defaultGen.nextLong(max);
            defaultGen.nextLong(min, max);
        }
    }
}