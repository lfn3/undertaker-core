package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.UserDebug;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class MultiShrinker implements ShrinkingByteSource {
    private final Iterator<Function<byte[], ShrinkingByteSource>> shrinkerConstructors;

    private ShrinkingByteSource current;

    public static MultiShrinker fromShrinkers(byte[] startingWith, Function<byte[], ShrinkingByteSource>... shrinkerConstructors) {
        UserDebug.userAssert(shrinkerConstructors.length > 0, "Must pass at least one shrinker into a multishrinker");

        ShrinkingByteSource first = shrinkerConstructors[0].apply(startingWith);
        return new MultiShrinker(first, Arrays.asList(shrinkerConstructors).subList(1, shrinkerConstructors.length).iterator());
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
