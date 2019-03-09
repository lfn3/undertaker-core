package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;
import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.junit.Test;

public class LongsTest {
    private static final int ITERATIONS = 10000;

    private final Intervals intervals = new Intervals(1);
    private final ByteSource randomSource = new WrappedRandomByteSource();
    private final Booleans booleans = new Booleans(randomSource, intervals);
    private final Integers integers = new Integers(randomSource, intervals, booleans);
    private final Longs defaultGen = new Longs(randomSource, intervals);

    @Test
    public void generateLongs() {
        defaultGen.next();
    }

    @Test
    public void generateLongsInRange() {
        for (int i = 0; i < ITERATIONS; i++) {
            final long max = defaultGen.next(Long.MAX_VALUE - 1);
            final long min = defaultGen.next(max);
            defaultGen.next(min, max);
        }
    }

    @Test
    public void generateLongArrays() {
        for (int i = 0; i < ITERATIONS; i++) {
            defaultGen.nextArray();
        }
    }

    @Test
    public void generateSizedLongArrays() {
        for (int i = 0; i < ITERATIONS; i++) {
            final int max = integers.next(1, Longs.DEFAULT_MAX_LENGTH);
            final int min = integers.next(0, max);
            defaultGen.nextArray(min, max);
        }
    }

    @Test
    public void testFill() {
        for (int i = 0; i < ITERATIONS; i++) {
            final long[] arr = new long[64];
            defaultGen.fill(arr);
        }
    }
}