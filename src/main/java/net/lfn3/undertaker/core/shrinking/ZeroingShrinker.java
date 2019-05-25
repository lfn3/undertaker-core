package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ZeroingShrinker implements ShrinkingByteSource {
    private int nextSearchIdx;

    private int bytesOffset = 0;
    private byte[] bytes;
    private byte[] backup;

    public ZeroingShrinker(final byte[] bytes) {
        this.bytes = bytes;
        this.nextSearchIdx = 0;

        next();
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
        final int nextToZeroIdx = ShrinkUtil.firstNonZeroByteIndex(bytes, nextSearchIdx);
        if (nextToZeroIdx == -1) {
            nextSearchIdx = -1;
            return;
        }
        bytes[nextToZeroIdx] = 0;
        nextSearchIdx =  nextToZeroIdx + 1;
    }

    @Override
    public boolean isExhausted() {
        return nextSearchIdx == -1;
    }

    @Override
    public void revertShrink() {
        bytesOffset = 0;
        bytes = Arrays.copyOf(backup, backup.length);
    }
}
