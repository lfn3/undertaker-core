package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.source.ByteSource;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CollectingByteSource implements ByteSource {
    public static final int STARTING_SIZE = 1024;
    private ByteSource wrap;
    private byte[] collect;
    private int pointer = 0;

    public CollectingByteSource(ByteSource wrap) {
        this.wrap = wrap;
        collect = new byte[STARTING_SIZE];
    }

    public byte[] getCollected() {
        return Arrays.copyOf(collect, pointer);
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges) {
        final ByteBuffer buf = wrap.nextBytes(ranges);

        copyBytesFromBuffer(buf);

        return buf;
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges, int repeat) {
        final ByteBuffer buf = wrap.nextBytes(ranges, repeat);

        copyBytesFromBuffer(buf);

        return buf;
    }

    private void copyBytesFromBuffer(ByteBuffer buf) {
        final int numberOfBytes = buf.remaining();

        if (collect.length < pointer + numberOfBytes) {
            final int newLength = Math.max(collect.length * 2, pointer + numberOfBytes);
            byte[] newCollect = new byte[newLength];
            System.arraycopy(collect, 0, newCollect, 0, pointer);
            collect = newCollect;
        }

        buf.get(collect, pointer, numberOfBytes);
        buf.rewind();

        pointer += numberOfBytes;
    }

    @Override
    public void reset() {
        wrap.reset();
        pointer = 0;
    }

    @Override
    public void next() {
        wrap.next();
        pointer = 0;
    }
}
