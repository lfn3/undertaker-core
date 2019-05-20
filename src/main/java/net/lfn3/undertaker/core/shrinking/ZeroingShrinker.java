package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ZeroingShrinker implements ShrinkingByteSource {
    private int nextShrinkIdx;

    private int bytesOffset = 0;
    private byte[] bytes;
    private byte[] backup;

    public ZeroingShrinker(final byte[] bytes) {
        this.bytes = bytes;
        backup = Arrays.copyOf(bytes, bytes.length);
        bytes[0] = 0;
        nextShrinkIdx = 1;
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges, int repeat) {
        final int length = ranges.length * repeat;
        final ByteBuffer buf = ByteBuffer.wrap(bytes, bytesOffset, length).slice();
        bytesOffset += length;
        Bytes.moveIntoNearestRange(buf, ranges); //This might change the underlying bytes.
        return buf;
    }

    @Override
    public void reset() {
        bytesOffset = 0;
    }

    @Override
    public void next() {
        backup = Arrays.copyOf(bytes, bytes.length);
        bytes[nextShrinkIdx] = 0;
        nextShrinkIdx += 1;
    }

    @Override
    public void revertShrink() {
        bytesOffset = 0;
        bytes = Arrays.copyOf(backup, backup.length);
    }
}
