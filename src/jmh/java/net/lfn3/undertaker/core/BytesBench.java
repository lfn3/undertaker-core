package net.lfn3.undertaker.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.nio.ByteBuffer;
import java.util.Random;

@State(Scope.Benchmark)
public class BytesBench {
    private static final int SIZE = Long.BYTES;
    private static final ByteBuffer[] BUFFERS = new ByteBuffer[SIZE];
    private static final Ranges[] RANGES = new Ranges[SIZE];

    @Setup
    public void setup() {
        Random rand = new Random();
        for (int i = 0; i < SIZE; i++) {
            byte[] wrapped = new byte[SIZE];
            BUFFERS[i] = ByteBuffer.wrap(wrapped);
            rand.nextBytes(wrapped);

            byte[] range = new byte[SIZE * 4];
            rand.nextBytes(range);

            range[0] = -128;
            range[SIZE] = -1;
            range[SIZE * 2] = 0;
            range[SIZE * 3] = 127;

            RANGES[i] = Ranges.fromFlatArray(range, SIZE);
        }
    }

    @Benchmark
    public ByteBuffer[] moveIntoRangeBenchmark() {
        for (int i = 0; i < SIZE; i++) {
            Bytes.moveIntoRange(BUFFERS[i], RANGES[i]);
        }

        return BUFFERS;
    }

    @Benchmark
    public ByteBuffer moveIntoRangeSingleBenchmark() {
        Bytes.moveIntoRange(BUFFERS[0], RANGES[0]);
        return BUFFERS[0];
    }

    public static void main(String[] args) {
        final BytesBench bench = new BytesBench();
        bench.setup();
        bench.moveIntoRangeBenchmark();
        bench.moveIntoRangeSingleBenchmark();
    }
}
