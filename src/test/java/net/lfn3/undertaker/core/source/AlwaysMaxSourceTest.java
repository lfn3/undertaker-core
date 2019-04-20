package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Ranges;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AlwaysMaxSourceTest {
    private static final int RANGE_LENGTH = Long.BYTES;

    @Test
    public void shouldReturnMax() {
        final byte[] negLower = new byte[RANGE_LENGTH];
        Arrays.fill(negLower, (byte) -128);

        final byte[] negUpper = new byte[RANGE_LENGTH];
        Arrays.fill(negUpper, (byte) -1);

        final byte[] posLower = new byte[RANGE_LENGTH];
        Arrays.fill(posLower, (byte) 0);

        final byte[] posUpper = new byte[RANGE_LENGTH];
        Arrays.fill(posUpper, (byte) -1);
        posUpper[0] = 127;

        final Ranges ranges = Ranges.fromArrays(negLower, negUpper, posLower, posUpper);
        final ByteBuffer buf = new AlwaysMaxSource().nextBytes(ranges);

        assertEquals(Long.MAX_VALUE, buf.getLong());
    }
}