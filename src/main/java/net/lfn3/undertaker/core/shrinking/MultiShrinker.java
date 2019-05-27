package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.UserDebug;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Function;

public class MultiShrinker implements ShrinkingByteSource {
    private final Iterator<Function<byte[], ShrinkingByteSource>> shrinkerConstructors;

    private ShrinkingByteSource current;

    public static MultiShrinker fromShrinkers(byte[] startingWith, Iterable<Function<byte[], ShrinkingByteSource>> shrinkerConstructors) {
        final Iterator<Function<byte[], ShrinkingByteSource>> iter = shrinkerConstructors.iterator();
        UserDebug.userAssert(iter.hasNext(), "Must pass at least one shrinker into a multishrinker");

        ShrinkingByteSource first = iter.next().apply(startingWith);
        return new MultiShrinker(first, iter);
    }

    private MultiShrinker(ShrinkingByteSource current, Iterator<Function<byte[], ShrinkingByteSource>> restConstructors) {
        this.shrinkerConstructors = restConstructors;
        this.current = current;
    }

    @Override
    public void revertShrink() {
        current.revertShrink();
    }

    @Override
    public boolean isExhausted() {
        return !shrinkerConstructors.hasNext() && current.isExhausted();
    }

    @Override
    public void confirmShrink() {
        current.confirmShrink();
    }

    @Override
    public void rejectShrink() {
        current.rejectShrink();
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges, int repeat) {
        return current.nextBytes(ranges, repeat);
    }

    @Override
    public ByteBuffer nextBytes(Ranges ranges) {
        return current.nextBytes(ranges);
    }

    @Override
    public ByteBuffer nextBytes(Range range) {
        return current.nextBytes(range);
    }

    @Override
    public void pregen(int bytesToPregen) {
        current.pregen(bytesToPregen);
    }

    @Override
    public void reset() {
        current.reset();
    }

    @Override
    public void next() {
        if (!current.isExhausted()) {
            current.next();
        } else if (shrinkerConstructors.hasNext()) {
            final byte[] shrunk = current.getBytes();
            current = shrinkerConstructors.next().apply(shrunk);
        } else {
            throw new IllegalStateException("All shrinkers exhausted");
        }
    }

    @Override
    public byte[] getBytes() {
        return current.getBytes();
    }
}
