package net.lfn3.undertaker.core.intervals;

import net.lfn3.undertaker.core.DevDebug;
import net.lfn3.undertaker.core.UserDebug;

import java.util.*;
import java.util.stream.Collectors;

import static net.lfn3.undertaker.core.intervals.IntervalFlag.*;

//TODO: might be worth splitting this up, particularly if we can separate the "fast" mode from everything else.
public class Intervals {
    private final Queue<Interval> intervalPool;
    private final Stack<Interval> intervalStack = new Stack<>();
    private final List<Interval> saved = new ArrayList<>();
    private IntervalsMode mode = IntervalsMode.FAST;

    public Intervals()
    {
        this(64);
    }

    public Intervals(final int startingSize) {
        intervalPool = new ArrayDeque<>(startingSize);
        for (int i = 0; i < startingSize; i++) {
            intervalPool.add(new Interval()); //Not thread safe, we're leaking this
        }
    }

    void setMode(final IntervalsMode mode) {
        assert mode != null;
        this.mode = mode;

        saved.forEach(i -> {
            i.reset();
            intervalPool.add(i);
        });

        saved.clear();
    }

    public Interval next() {
        return next(NONE);
    }

    public Interval next(final EnumSet<IntervalFlag> flags) {
        for (Interval parent : intervalStack) {
            if (parent.hasFlag(IntervalFlag.SNIPPABLE_CHILDREN))
            {
                flags.add(IntervalFlag.SNIPPABLE);
            }
            if (parent.hasFlag(IntervalFlag.UNIQUE_CHILDREN))
            {
                flags.add(IntervalFlag.UNIQUE);
            }
        }

        if (!issueInterval(flags))
        {
            return Interval.NONE;
        }

        Interval interval = intervalPool.poll();
        if (interval == null) {
            interval = new Interval();
        }

        interval.populate(flags);

        intervalStack.push(interval);

        return interval;
    }

    private boolean issueInterval(final EnumSet<IntervalFlag> flags) {
        final boolean unique = flags.contains(UNIQUE_CHILDREN);
        final boolean shrinking = mode == IntervalsMode.SHRINK && (flags.contains(SNIPPABLE) || flags.contains(SNIPPABLE_CHILDREN));
        final boolean display = needGeneratedObject();

        return unique || shrinking || display;
    }

    private boolean saveInterval(final EnumSet<IntervalFlag> flags) {
        final boolean shrinking = mode == IntervalsMode.SHRINK && flags.contains(SNIPPABLE);
        return shrinking || needGeneratedObject();
    }

    private boolean needGeneratedObject() {
        final boolean retainForDisplay = mode == IntervalsMode.DISPLAY && intervalStack.isEmpty();
        final boolean retainForDebug = mode == IntervalsMode.DEBUG;
        return retainForDisplay || retainForDebug;
    }

    public void done(final Interval interval, Object generatedValue) {
        if (interval == Interval.NONE)
        {
            return;
        }

        //TODO: can we validate if a composite interval had no children?
        Interval popped = intervalStack.pop();

        UserDebug.userAssert(popped == interval,
                "Interval popped off in the wrong order, we expected to be done with " + popped + " next.");

        if (saveInterval(interval.flags))
        {
            saved.add(interval);
        }
        if (needGeneratedObject()) {
            interval.retain(generatedValue);
        }
    }

    public Collection<Object> getGeneratedValues() {
        DevDebug.devAssert(mode != IntervalsMode.FAST,
                "No point in trying to get the generated values back out if nothing has been saved");

        return saved.stream().map(Interval::getGeneratedValue).collect(Collectors.toList());
    }
}
