package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import static net.lfn3.undertaker.core.Debug.devAssert;

public class WrappedRandomByteSource implements ByteSource {
    private static final int DEFAULT_PREGEN_SIZE = 8 * 1024;

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
        final boolean enoughPregennedLeft = ranges.length <= pregenned.length - pointer;
        if (!enoughPregennedLeft) {
            pregenned = new byte[Math.max(4 * ranges.length, 2 * pregenned.length)];
            wrapped.nextBytes(pregenned);
            pointer = 0;
        }

        final ByteBuffer buf = ByteBuffer.wrap(pregenned, pointer, ranges.length).slice();

        Bytes.moveIntoAnyRange(buf, ranges);

        devAssert(() -> ranges.isIn(Arrays.copyOfRange(pregenned, pointer, pointer + ranges.length)),
                "Move into any range should have pushed this value into a supplied range");

        pointer += ranges.length;

        return buf; //TODO: asReadOnlyBuffer?
    }

    public void reset() {
        wrapped.setSeed(seed);
    }

    public void next() {
        seed = wrapped.nextLong();
        wrapped.setSeed(seed);
    }
}
