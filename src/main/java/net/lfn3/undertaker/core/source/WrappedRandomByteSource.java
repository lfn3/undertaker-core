package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.Random;

import static net.lfn3.undertaker.core.Debug.devAssert;

public class WrappedRandomByteSource implements ByteSource {
    private final Random wrapped;
    private long seed;

    public WrappedRandomByteSource() {
        wrapped = new Random();
        seed = wrapped.nextLong();
        wrapped.setSeed(seed);
    }

    public WrappedRandomByteSource(final long seed) {
        wrapped = new Random(seed);
        this.seed = seed;
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges) {
        byte[] unmapped = new byte[ranges.length];
        wrapped.nextBytes(unmapped);
        ByteBuffer buf = ByteBuffer.wrap(unmapped);

        Bytes.moveIntoAnyRange(buf, ranges);

        devAssert(() -> ranges.isIn(unmapped), "Move into any range should have pushed this value into a supplied range");

        return buf;
    }

    public void reset() {
        wrapped.setSeed(seed);
    }

    public void next() {
        seed = wrapped.nextLong();
        wrapped.setSeed(seed);
    }
}
