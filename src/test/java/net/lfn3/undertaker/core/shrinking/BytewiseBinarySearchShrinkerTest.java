package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.generators.Booleans;
import net.lfn3.undertaker.core.generators.Integers;
import net.lfn3.undertaker.core.intervals.Intervals;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class BytewiseBinarySearchShrinkerTest {
    @Test
    public void shouldHalveFirstByteOfInput() {
        byte[] arr = new byte[]{127, 0, 0, 0};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        Assert.assertEquals(1056964608, ints.next());
    }

    @Test
    public void shouldIgnoreLeadingZeroBytes() {
        byte[] arr = new byte[]{0, 0, 0, 127};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        Assert.assertEquals(63, ints.next());
    }

    @Test
    public void shouldBackUpTowardsInitialValueIfFirstShrinkIsReset() {
        byte[] arr = new byte[]{0, 0, 0, 127};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        shrinker.rejectShrink();

        Assert.assertEquals(95, ints.next());
    }

    @Test
    public void shouldStopBackingOffIfShrinkConfirmed() {
        byte[] arr = new byte[]{0, 0, 0, 127};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        shrinker.rejectShrink();
        shrinker.confirmShrink();

        Assert.assertEquals(79, ints.next());
    }

    @Test
    public void shouldContinueTowardsZeroValueIfFirstShrinkIsConfirmed() {
        byte[] arr = new byte[]{0, 0, 0, 127};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        shrinker.confirmShrink();

        Assert.assertEquals(31, ints.next());
    }

    @Test
    public void shouldAdvanceToNextByte() {
        byte[] arr = new byte[]{0, 0, 1, 127};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        Assert.assertEquals(127, ints.next());

        shrinker.confirmShrink();

        Assert.assertEquals(63, ints.next());
    }

    @Test
    public void shouldHandleNegativeBytes() {
        byte[] arr = new byte[]{0, 0, 0, -1};

        ShrinkingByteSource shrinker = new BytewiseBinarySearchShrinker(arr);

        final Intervals intervals = new Intervals();
        final Booleans booleans = new Booleans(shrinker, intervals);
        final Integers ints = new Integers(shrinker, intervals, booleans);

        Assert.assertEquals(127, ints.next());

        shrinker.rejectShrink();

        //ByteBuffer.wrap(new byte[]{0, 0, 0, -64}).getInt() == 191
        Assert.assertEquals(191, ints.next());

        shrinker.confirmShrink();

        Assert.assertEquals(159, ints.next());
    }
}