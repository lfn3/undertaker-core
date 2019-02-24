package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Ranges;
import net.lfn3.undertaker.core.SourceRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

public class WrappedRandomByteSourceTest {
    @Rule
    public final SourceRule sourceRule = new SourceRule();

    @Test
    public void exercise() {
        //Relying on the assertions.
        final int iterations = 1000;
        final int rangeLength = 5;

        for (int i = 0; i < iterations; i++) {
            Ranges r = generateRanges(rangeLength);

            WrappedRandomByteSource wrs = new WrappedRandomByteSource();

            wrs.nextBytes(r);
        }
    }

    private Ranges generateRanges(final int rangeLength) {
        final byte[] negLower = new byte[rangeLength];
        Arrays.fill(negLower, (byte) -128);

        final byte[] negUpper = new byte[rangeLength];
        Arrays.fill(negUpper, (byte) -1);

        final byte[] posLower = new byte[rangeLength];
        Arrays.fill(posLower, (byte) 0);

        final byte[] posUpper = new byte[rangeLength];
        Arrays.fill(posUpper, (byte) 127);

        return Ranges.fromArrays(negLower, negUpper, posLower, posUpper);
    }
}