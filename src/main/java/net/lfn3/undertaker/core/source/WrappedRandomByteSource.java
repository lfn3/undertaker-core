package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.Random;

public class WrappedRandomByteSource implements ByteSource {
    private final Random wrapped;

    public WrappedRandomByteSource() {
        wrapped = new Random();
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges) {
        byte[] unmapped = new byte[ranges.length];
        wrapped.nextBytes(unmapped);
        ByteBuffer buf = ByteBuffer.wrap(unmapped);

        Bytes.moveIntoAnyRange(buf, ranges);

        return buf;
    }
}
