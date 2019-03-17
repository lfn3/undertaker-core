package net.lfn3.undertaker.core.generators;

import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.intervals.Interval;
import net.lfn3.undertaker.core.intervals.IntervalType;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.ByteSource;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.function.Function;

public class Collections {
    private ByteSource byteSource;
    private final Intervals intervals;
    private Integers integers;

    public Collections(ByteSource byteSource, Intervals intervals, Integers integers) {
        this.byteSource = byteSource;
        this.intervals = intervals;
        this.integers = integers;
    }

    public <T> T[] array(Class<T> tClass, int minSize, int maxSize, Ranges r, Function<ByteBuffer, T> conversionFn) {
        final Interval collInterval = intervals.next(IntervalType.COLLECTION);
        final int size = getSize(minSize, maxSize);

        @SuppressWarnings("unchecked")
        final T[] out = (T[]) Array.newInstance(tClass, size);

        for (int i = 0; i < size; i++) {
            final Interval elemInterval = intervals.next(IntervalType.SNIPPABLE);
            final T val = conversionFn.apply(byteSource.nextBytes(r));
            out[i] = val;
            intervals.done(elemInterval, val);
        }

        intervals.done(collInterval, out);

        return out;
    }

    int getSize(int minSize, int maxSize) {
        final Interval collSizeInterval = intervals.next(IntervalType.COLLECTION_SIZE);

        final int size = integers.next(minSize, maxSize);

        intervals.done(collSizeInterval, size);
        return size;
    }

    public int[] intArray(int minSize, int maxSize, Ranges r) {
        final Interval collInterval = intervals.next(IntervalType.COLLECTION);
        final int size = getSize(minSize, maxSize);
        byteSource.pregen(r.length * size);

        final int[] out = new int[size];

        for (int i = 0; i < size; i++) {
            final Interval elemInterval = intervals.next(IntervalType.SNIPPABLE);
            final int val = byteSource.nextBytes(r).getInt(0);
            out[i] = val;
            intervals.done(elemInterval, val);
        }

        intervals.done(collInterval, out);

        return out;
    }

}
