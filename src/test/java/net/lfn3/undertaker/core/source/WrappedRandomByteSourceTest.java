package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Ranges;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class WrappedRandomByteSourceTest {
    private static final int ITERATIONS = 10000;
    private static final int RANGE_LENGTH = 128;
    private static final Ranges RANGES = generateRanges();

    @Test
    public void exercise() {
        //Relying on the assertions.
        for (int i = 0; i < ITERATIONS; i++) {
            new WrappedRandomByteSource().nextBytes(RANGES);
        }
    }

    @Test
    public void shouldProduceSameBytesBeforeAndAfterReset() {
        for (int i = 0; i < ITERATIONS; i++) {
            WrappedRandomByteSource wrs = new WrappedRandomByteSource();
            final ByteBuffer beforeReset = wrs.nextBytes(RANGES);
            wrs.reset();
            final ByteBuffer afterReset = wrs.nextBytes(RANGES);
            Assert.assertArrayEquals(beforeReset.array(), afterReset.array());
        }
    }

    @Test
    public void shouldNotProudceSameBytesAfterNext() {
        WrappedRandomByteSource wrs = new WrappedRandomByteSource();
        ByteBuffer beforeNext = wrs.nextBytes(RANGES);
        for (int i = 0; i < ITERATIONS; i++) {
            wrs.next();
            final ByteBuffer afterNext = wrs.nextBytes(RANGES);
            Assert.assertThat(beforeNext, IsNot.not(IsEqual.equalTo(afterNext)));
            beforeNext = afterNext;
        }
    }

    private static Ranges generateRanges() {
        final byte[] negLower = new byte[RANGE_LENGTH];
        Arrays.fill(negLower, (byte) -128);

        final byte[] negUpper = new byte[RANGE_LENGTH];
        Arrays.fill(negUpper, (byte) -1);

        final byte[] posLower = new byte[RANGE_LENGTH];
        Arrays.fill(posLower, (byte) 0);

        final byte[] posUpper = new byte[RANGE_LENGTH];
        Arrays.fill(posUpper, (byte) 127);

        return Ranges.fromArrays(negLower, negUpper, posLower, posUpper);
    }
}