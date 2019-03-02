package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;
import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.junit.Test;

public class IntegersTest {
    private static final int ITERATIONS = 10000;

    private final Intervals intervals = new Intervals(1);
    private final ByteSource randomSource = new WrappedRandomByteSource();
    private final Booleans booleans = new Booleans(randomSource, intervals);
    private final Integers defaultGen = new Integers(randomSource, intervals, booleans);

    @Test
    public void generateInts()
    {
        for (int i = 0; i < ITERATIONS; i++) {
            defaultGen.next();
        }
    }

    @Test
    public void generateIntsInRange() {
        for (int i = 0; i < ITERATIONS; i++) {
            final int max = defaultGen.next(Integer.MAX_VALUE - 1);
            final int min = defaultGen.next(max);

            defaultGen.next(min, max);
        }
    }

    @Test
    public void generateIntArrays() {
        for (int i = 0; i < ITERATIONS; i++) {
            defaultGen.nextArray();
        }
    }
}