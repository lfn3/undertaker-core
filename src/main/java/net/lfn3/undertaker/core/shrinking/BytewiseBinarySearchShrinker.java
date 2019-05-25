package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Bytes;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static net.lfn3.undertaker.core.shrinking.ShrinkUtil.firstNonZeroByteIndex;

public class BytewiseBinarySearchShrinker implements ShrinkingByteSource {
    private int nextShrinkIdx;

    private byte lastShrink;
    private byte high;
    private byte low;
    private boolean backoff;

    private int bytesOffset = 0;
    private byte[] bytes;
    private byte[] backup;

    public BytewiseBinarySearchShrinker(final byte[] bytes) {
        nextShrinkIdx = firstNonZeroByteIndex(bytes);

        this.bytes = bytes;
        backup = Arrays.copyOf(bytes, bytes.length);

        initIdx();
    }

    private void initIdx() {
        if (nextShrinkIdx == -1) {
            return;
        }
        high = bytes[nextShrinkIdx];
        low = 0;
        lastShrink = (byte) ((0xff & bytes[nextShrinkIdx]) / 2);
        bytes[nextShrinkIdx] = lastShrink;
        backoff = false;
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
        backoff = false;
        high = lastShrink;
        bytesOffset = 0;
    }

    @Override
    public void next() {
        if (nextShrinkIdx == -1) {
            throw new IllegalStateException("This shrinker has exhausted the shrinking it can perform");
        }

        backup = Arrays.copyOf(bytes, bytes.length);

        if (low == high) {
            nextShrinkIdx = firstNonZeroByteIndex(bytes, nextShrinkIdx + 1);
            initIdx();
        } else {
            if (backoff) {
                final byte next = (byte) (lastShrink + ((0xff & high) - lastShrink) / 2);
                bytes[nextShrinkIdx] = next;
                lastShrink = next;
            } else {
                final byte next = (byte) (low + (0xff & (lastShrink - (0xff & low)))  / 2);
                bytes[nextShrinkIdx] = next;
                lastShrink = next;
            }
        }
    }

    @Override
    public boolean isExhausted() {
        return nextShrinkIdx == -1;
    }

    @Override
    public void revertShrink() {
        backoff = true;
        low = lastShrink;
        bytes = Arrays.copyOf(backup, backup.length);
        bytesOffset = 0;
    }
}
