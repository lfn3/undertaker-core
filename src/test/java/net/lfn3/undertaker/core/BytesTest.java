package net.lfn3.undertaker.core;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class BytesTest {
    @Test
    public void castingBetweenBytesAndInts() {
        assertEquals(255, (0xff & (byte) -1));
        assertEquals(128, (0xff & (byte) -128));
        assertEquals(-1, (byte) 255);
    }

    @Test
    public void moveIntoRangeShouldDoNothingIfAlreadyInARange() {
        final byte[] input = {1, 2, 3};

        ByteBuffer b = ByteBuffer.wrap(input);

        final byte[] ranges = {
                0, 1, 2,
                3, 4, 5
        };

        Bytes.moveIntoRange(b, Ranges.fromFlatArray(ranges, 3));

        Assert.assertArrayEquals(input, b.array());
    }

    @Test
    public void moveIntoNearestRangeShouldDoNothingIfAlreadyInARange() {
        final byte[] input = {1, 2, 3};

        ByteBuffer b = ByteBuffer.wrap(input);

        final byte[] ranges = {
                0, 1, 2,
                3, 4, 5
        };

        Bytes.moveIntoNearestRange(b, Ranges.fromFlatArray(ranges, 3));

        Assert.assertArrayEquals(input, b.array());
    }

    @Test
    public void moveIntoRangeRepeat() {
        final byte[] input = {1, 2, 3};

        ByteBuffer b = ByteBuffer.wrap(input);

        final byte[] ranges = {
                0,
                1,
        };

        Bytes.moveIntoRange(b, Ranges.fromFlatArray(ranges, 1));

        Assert.assertArrayEquals(new byte[]{1, 0, 1}, b.array());
    }

    @Test
    public void moveIntoNearestRangeRepeat() {
        final byte[] input = {1, 2, 3};

        ByteBuffer b = ByteBuffer.wrap(input);

        final byte[] ranges = {
                0,
                1,
        };

        Bytes.moveIntoNearestRange(b, Ranges.fromFlatArray(ranges, 1));

        Assert.assertArrayEquals(new byte[]{1, 0, 1}, b.array());
    }

}