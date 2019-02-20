package net.lfn3.undertaker.core.intervals;

import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class IntervalsTest {
    private Intervals intervals = new Intervals(1);

    @Test
    public void reusesIntervals() {
        final Interval interval = intervals.next(IntervalType.COMPOSITE);

        intervals.done(interval, null); //Hands back interval

        assertSame(interval, intervals.next(IntervalType.VALUE));
    }

    @Test
    public void clearsIntervalOnDone() {
        final Interval interval = intervals.next(IntervalType.VALUE);

        intervals.done(interval, null);

        assertNull(interval.getType());
    }

    @Test
    public void retainsSnippableIntervalsWhenShrinking() {
        intervals.setMode(IntervalsMode.SHRINK);

        final Interval one = intervals.next(IntervalType.COMPOSITE, true);
        final Interval child = intervals.next(IntervalType.COMPOSITE);

        intervals.done(child, null);
        intervals.done(one, null);

        final Object[] generatedValues = intervals.getGeneratedValues().toArray();
        assertArrayEquals(new Object[]{ null }, generatedValues); //We don't hang onto the generated object

        assertSame(one, intervals.next(IntervalType.VALUE)); //But we won't serve back up the child interval either.
    }

    @Test
    public void shouldGetTopLevelGeneratedValuesWhenDisplaying() {
        intervals.setMode(IntervalsMode.DISPLAY);

        final Interval parentOne = intervals.next(IntervalType.COMPOSITE);
        final Interval childOne = intervals.next(IntervalType.VALUE);

        intervals.done(childOne, null);
        intervals.done(parentOne, "parentOne");

        final Interval parentTwo = intervals.next(IntervalType.VALUE);

        intervals.done(parentTwo, "parentTwo");

        final Object[] generatedValues = intervals.getGeneratedValues().toArray();

        assertArrayEquals(new Object[]{ "parentOne", "parentTwo" }, generatedValues);
    }
}