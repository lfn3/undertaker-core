package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;

public interface ByteSource {
    ByteBuffer nextBytes(Ranges ranges);

    /**
     * @param range Range describing the length and breadth of values we can generate
     * @return a ByteBuffer containing a value within the supplied range.
     */
    default ByteBuffer nextBytes(Range range) {
        return nextBytes(Ranges.fromRange(range));
    }
}
