package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.nio.ByteBuffer;
import java.util.Random;

@State(Scope.Benchmark)
public class ByteSourceBench {
    private static final int SIZE = Long.BYTES;
    private static Ranges RANGES;

    private WrappedRandomByteSource wrs;

    @Setup
    public void setup() {
        wrs = new WrappedRandomByteSource();
        Random rand = new Random();
        byte[] range = new byte[SIZE * 4];
        rand.nextBytes(range);

        range[0] = -128;
        range[SIZE] = -1;
        range[SIZE * 2] = 0;
        range[SIZE * 3] = 127;

        RANGES = Ranges.fromFlatArray(range, SIZE);
    }

    @Benchmark
    public ByteBuffer wrappedRandomByteSource() {
        return wrs.nextBytes(RANGES);
    }
}
