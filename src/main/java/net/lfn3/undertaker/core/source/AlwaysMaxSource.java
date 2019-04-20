package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;

public class AlwaysMaxSource implements ByteSource {
    @Override
    public ByteBuffer nextBytes(Ranges ranges, int repeat) {
        int len = ranges.length * repeat;
        final ByteBuffer buf = ByteBuffer.wrap(new byte[len]);
        final ByteBuffer uppermostRange = ranges.get(ranges.numberOfRanges - 1).get(Range.Bound.UPPER);

        for (int i = 0; i < repeat; i++) {
            buf.put(uppermostRange);
        }
        buf.rewind();

        return buf;
    }

    @Override
    public void reset() {

    }

    @Override
    public void next() {

    }
}
