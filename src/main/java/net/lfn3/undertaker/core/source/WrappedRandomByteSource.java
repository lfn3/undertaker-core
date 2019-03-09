package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static net.lfn3.undertaker.core.Debug.devAssert;

public class WrappedRandomByteSource implements ByteSource {
    private static final int DEFAULT_PREGEN_SIZE = 8 * 1024;
    public static final int DEFAULT_RANGE_BATCH_SIZE = 32;

    private final Random wrapped;
    private long seed;

    private byte[] pregenned;
    private int pointer;

    public WrappedRandomByteSource() {
        this(new Random().nextLong());
    }

    public WrappedRandomByteSource(final long seed) {
        wrapped = new Random(seed);
        this.seed = seed;

        pregenned = new byte[DEFAULT_PREGEN_SIZE];
        wrapped.nextBytes(pregenned);
        pointer = 0;
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges) {
        ensurePregenned(ranges.length);

        final ByteBuffer buf = ByteBuffer.wrap(pregenned, pointer, ranges.length).slice();

        Bytes.moveIntoAnyRange(buf, ranges);

        devAssert(() -> ranges.isIn(Arrays.copyOfRange(pregenned, pointer, pointer + ranges.length)),
                "Move into any range should have pushed this value into a supplied range");

        pointer += ranges.length;

        return buf; //TODO: asReadOnlyBuffer?
    }

    @Override
    public ByteBuffer nextBytes(Range range) {
        ensurePregenned(range.length);

        final ByteBuffer buf = ByteBuffer.wrap(pregenned, pointer, range.length).slice();

        Bytes.moveIntoRange(buf, range);

        devAssert(() -> range.isIn(Arrays.copyOfRange(pregenned, pointer, pointer + range.length)),
                "Move into any range should have pushed this value into a supplied range");

        pointer += range.length;

        return buf; //TODO: asReadOnlyBuffer?
    }

    @Override
    public Collection<ByteBuffer> nextBytes(Ranges ranges, int repeat) {
        ensurePregenned(ranges.length * repeat);

        Bytes.moveIntoRanges(pregenned, pointer, ranges, repeat);


        final List<ByteBuffer> ret = new ArrayList<>(repeat);
        for (int i = 0; i < repeat; i++) {
            ret.add(ByteBuffer.wrap(pregenned, i * ranges.length, ranges.length));
        }

        pointer += ranges.length * repeat;
        return ret;
    }

    private void ensurePregenned(int length) {
        final boolean enoughPregennedLeft = length <= remainingPregenned();
        if (!enoughPregennedLeft) {
            pregenned = new byte[Math.max(4 * length, 2 * pregenned.length)];
            wrapped.nextBytes(pregenned);
            pointer = 0;
        }
    }

    private int remainingPregenned() {
        return pregenned.length - pointer;
    }

    @Override
    public void pregen(int bytesToPregen) {
        ensurePregenned(bytesToPregen);
    }

    @Override
    public Stream<ByteBuffer> takeWhile(Ranges ranges, BiPredicate<ByteBuffer, Integer> predicate) {
        final Stream.Builder<ByteBuffer> streamBuilder = Stream.builder();
        int stopAt = -1;
        int produced = 0;
        do {
            final int iterations = remainingPregenned() / ranges.length;
            Bytes.moveIntoRanges(pregenned, pointer, ranges, iterations);
            for (int i = 0; i < iterations; i++) {
                final ByteBuffer buf = ByteBuffer.wrap(pregenned, pointer + i * ranges.length, ranges.length).slice();
                if (!predicate.test(buf, produced)) {
                    stopAt = i;
                    break;
                }

                streamBuilder.add(buf);
                produced++;
            }

            pregenned = new byte[ranges.length * DEFAULT_RANGE_BATCH_SIZE];
            wrapped.nextBytes(pregenned);
            pointer = 0;
        } while (stopAt == -1);

        return streamBuilder.build();
    }

    public void reset() {
        wrapped.setSeed(seed);
    }

    public void next() {
        seed = wrapped.nextLong();
        wrapped.setSeed(seed);
    }
}
