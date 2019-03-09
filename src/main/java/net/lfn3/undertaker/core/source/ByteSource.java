package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public interface ByteSource {
    ByteBuffer nextBytes(Ranges ranges);

    /**
     * This can be implemented as a performance optimization.
     *
     * @param range Range describing the length and breadth of values we can generate
     * @return a ByteBuffer containing a value within the supplied range.
     */
    default ByteBuffer nextBytes(Range range) {
        return nextBytes(Ranges.fromRange(range));
    }

    default Collection<ByteBuffer> nextBytes(Ranges ranges, int repeat) {
        List<ByteBuffer> ret = new ArrayList<>(repeat);
        for (int i = 0; i < repeat; i++) {
            ret.add(nextBytes(ranges));
        }

        return ret;
    }

    /**
     *
     * @param bytesToPregen number of bytes you expect to need
     */
    default void pregen(int bytesToPregen) {

    }

    //TODO: this could be improved by using `takeWhile` from Java 9. Need to put together a multi version build for that.
    default Stream<ByteBuffer> takeWhile(Ranges ranges, BiPredicate<ByteBuffer, Integer> predicate) {
        final Iterator<ByteBuffer> iter = Stream.generate(() -> nextBytes(ranges)).iterator();
        final Stream.Builder<ByteBuffer> builder = Stream.builder();

        for (int i = 0; true; i++) {
            ByteBuffer next = iter.next();

            if (!predicate.test(next, i)) {
                break;
            }

            builder.accept(next);
        }

        return builder.build();
    }
}
