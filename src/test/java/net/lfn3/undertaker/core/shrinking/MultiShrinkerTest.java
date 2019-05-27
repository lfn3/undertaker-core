package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.generators.Booleans;
import net.lfn3.undertaker.core.generators.Integers;
import net.lfn3.undertaker.core.intervals.Intervals;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class MultiShrinkerTest {
    @Test
    public void shouldBeAbleToMultishrink() {
        MultiShrinker multiShrinker = MultiShrinker.fromShrinkers(
                new byte[]{0, 0, 0, 3},
                Arrays.asList(ZeroingShrinker::new, BytewiseBinarySearchShrinker::new));

        final Intervals intervals = new Intervals();
        Integers integers = new Integers(multiShrinker, intervals, new Booleans(multiShrinker, intervals));

        final int zero = integers.next();

        assertThat(zero, equalTo(0));
    }

    @Test
    public void shouldMoveOnToNextShrinker() {
        MultiShrinker multiShrinker = MultiShrinker.fromShrinkers(
                new byte[]{0, 0, 0, 3},
                Arrays.asList(ZeroingShrinker::new, BytewiseBinarySearchShrinker::new));

        final Intervals intervals = new Intervals();
        Integers integers = new Integers(multiShrinker, intervals, new Booleans(multiShrinker, intervals));

        //Zeroing shrinker
        final int zero = integers.next();
        assertThat(zero, equalTo(0));
        multiShrinker.rejectShrink();

        //Binary search shrinker.
        multiShrinker.next();
        assertThat(integers.next(), not(equalTo(3)));
    }
}