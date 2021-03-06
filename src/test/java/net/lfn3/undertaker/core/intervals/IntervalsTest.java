package net.lfn3.undertaker.core.intervals;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class IntervalsTest {
    private Intervals intervals = new Intervals(1);

    @Test
    public void reusesIntervals() {
        final Interval interval = intervals.next(EnumSet.of(IntervalFlag.COMPOSITE));

        intervals.done(interval, null); //Hands back interval

        assertSame(interval, intervals.next(EnumSet.of(IntervalFlag.VALUE)));
    }

    @Test
    public void retainsSnippableIntervalsWhenShrinking() {
        intervals.setMode(IntervalsMode.SHRINK);

        final Interval one = intervals.next(EnumSet.of(IntervalFlag.COMPOSITE, IntervalFlag.SNIPPABLE_CHILDREN));
        final Interval child = intervals.next(EnumSet.of(IntervalFlag.COMPOSITE));

        intervals.done(child, null);
        intervals.done(one, null);

        final Object[] generatedValues = intervals.getGeneratedValues().toArray();
        assertArrayEquals(new Object[]{ null }, generatedValues); //We don't hang onto the generated object
    }

    @Test
    public void getTopLevelGeneratedValuesWhenDisplaying() {
        intervals.setMode(IntervalsMode.DISPLAY);

        final Interval parentOne = intervals.next(EnumSet.of(IntervalFlag.COMPOSITE));
        final Interval childOne = intervals.next(EnumSet.of(IntervalFlag.VALUE));

        intervals.done(childOne, null);
        intervals.done(parentOne, "parentOne");

        final Interval parentTwo = intervals.next(EnumSet.of(IntervalFlag.VALUE));

        intervals.done(parentTwo, "parentTwo");

        final Object[] generatedValues = intervals.getGeneratedValues().toArray();

        assertArrayEquals(new Object[]{ "parentOne", "parentTwo" }, generatedValues);
    }
}