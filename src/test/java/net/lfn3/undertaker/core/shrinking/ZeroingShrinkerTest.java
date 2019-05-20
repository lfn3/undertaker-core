package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.generators.Booleans;
import net.lfn3.undertaker.core.generators.Integers;
import net.lfn3.undertaker.core.generators.Longs;
import net.lfn3.undertaker.core.intervals.Intervals;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class ZeroingShrinkerTest {
    @Test
    public void shouldZeroOutValue() {
        byte[] arr = new byte[Long.BYTES];

        new Random().nextBytes(arr);

        ShrinkingByteSource shrinker = new ZeroingShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Longs longs = new Longs(shrinker, intervals, new Integers(shrinker, intervals, booleans));

        for (int i = 0; i < Long.BYTES - 1; i++) {
            shrinker.next();
        }

        Assert.assertEquals(0, longs.next());
    }

    @Test
    public void shouldBeAbleToRevertToPreviousValue() {
        byte[] arr = new byte[Long.BYTES];

        new Random().nextBytes(arr);

        ShrinkingByteSource shrinker = new ZeroingShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Longs longs = new Longs(shrinker, intervals, new Integers(shrinker, intervals, booleans));

        final long first = longs.next();
        shrinker.reset();
        shrinker.next();
        final long second = longs.next();
        shrinker.revertShrink();
        final long firstAgain = longs.next();
        shrinker.reset();
        shrinker.next();
        final long third = longs.next();

        Assert.assertNotEquals(first, second);
        Assert.assertEquals(first, firstAgain);
        Assert.assertNotEquals(second, third);
    }
}
