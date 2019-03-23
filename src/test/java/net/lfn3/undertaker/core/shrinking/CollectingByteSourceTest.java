package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.generators.Booleans;
import net.lfn3.undertaker.core.generators.Integers;
import net.lfn3.undertaker.core.generators.Longs;
import net.lfn3.undertaker.core.intervals.Intervals;
import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CollectingByteSourceTest {
    @Test
    public void shouldCollectBytes() {
        WrappedRandomByteSource byteSource = new WrappedRandomByteSource();
        final CollectingByteSource collectingByteSource = new CollectingByteSource(byteSource);

        final int length = 6;
        final Ranges ranges = Ranges.fromFlatArray(new byte[]{
                1, 2, 3, 4, 5, 6,
                1, 2, 3, 4, 5, 6}, length);
        collectingByteSource.nextBytes(ranges);

        assertThat(collectingByteSource.getCollected(), equalTo(new byte[]{1, 2, 3, 4, 5, 6}));
    }

    @Test
    public void shouldCollectRandomBytes() {
        WrappedRandomByteSource byteSource = new WrappedRandomByteSource();
        final CollectingByteSource collectingByteSource = new CollectingByteSource(byteSource);

        final int length = 6;
        final Ranges ranges = Ranges.fromFlatArray(new byte[]{
                0, 0, 0, 0, 0, 0,
                1, 2, 3, 4, 5, 6}, length);
        final int repeat = 1024;
        byte[] generated = new byte[length * repeat];
        collectingByteSource.nextBytes(ranges, repeat).get(generated);

        assertThat(collectingByteSource.getCollected(), equalTo(generated));
    }

    @Test
    public void shouldCollectALotOfBytes() {
        WrappedRandomByteSource byteSource = new WrappedRandomByteSource();
        final CollectingByteSource collectingByteSource = new CollectingByteSource(byteSource);

        final Intervals intervals = new Intervals();
        final Longs longs = new Longs(collectingByteSource, intervals, new Integers(collectingByteSource, intervals, new Booleans(byteSource, intervals)));

        final int length = 2048;
        final long[] generated = longs.nextArray(length, length);
        final long[] collected = new long[length];

        final ByteBuffer buf = ByteBuffer.wrap(collectingByteSource.getCollected()).order(ByteOrder.BIG_ENDIAN);
        final int size = buf.getInt();
        buf.asLongBuffer().get(collected);

        assertThat(collected.length, equalTo(size));
        assertThat(collected, equalTo(generated));
    }
}