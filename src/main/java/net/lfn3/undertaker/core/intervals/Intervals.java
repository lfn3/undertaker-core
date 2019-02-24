package net.lfn3.undertaker.core.intervals;

import net.lfn3.undertaker.core.Debug;

import java.util.*;
import java.util.stream.Collectors;

//TODO: might be worth splitting this up, particularly if we can separate the "fast" mode from everything else.
public class Intervals {
    private final Queue<Interval> intervalPool;
    private final Stack<Interval> intervalStack = new Stack<>();
    private final List<Interval> saved = new ArrayList<>();
    private IntervalsMode mode = IntervalsMode.FAST;

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

    public Interval next(final IntervalType type) {
        return next(type, false);
    }

    public Interval next(final IntervalType type, boolean shouldMarkChildrenAsSnippable) {
        final boolean snippable;
        if (!intervalStack.isEmpty()) {
            final Interval parent = intervalStack.peek();
            Debug.userAssert(parent.getType() == IntervalType.COMPOSITE,
                    "You tried to add a child interval under a value interval: " + parent);
            snippable = parent.shouldMarkChildrenAsSnippable();
        } else {
            snippable = false;
        }

        Interval interval = intervalPool.poll();
        if (interval == null) {
            interval = new Interval();
        }

        interval.populate(type, shouldMarkChildrenAsSnippable, snippable);

        intervalStack.push(interval);

        return interval;
    }

    private boolean shouldRetainInterval(final Interval interval) {
        final boolean retainForShrinking = mode == IntervalsMode.SHRINK && interval.isSnippable();
        final boolean retainForDisplay = shouldRetainGeneratedObject();

        return retainForShrinking || retainForDisplay;
    }

    private boolean shouldRetainGeneratedObject() {
        final boolean retainForDisplay = mode == IntervalsMode.DISPLAY && intervalStack.isEmpty();
        final boolean retainForDebug = mode == IntervalsMode.DEBUG;
        return retainForDisplay || retainForDebug;
    }

    void done(final Interval interval, Object generatedValue) {
        //TODO: can we validate if a composite interval had no children?
        Interval popped = intervalStack.pop();

        Debug.userAssert(popped == interval,
                "Interval popped off in the wrong order, we expected to be done with " + popped + " next.");

        if (shouldRetainInterval(interval)) {
            saved.add(interval);
            if (shouldRetainGeneratedObject()) {
                interval.retain(generatedValue);
            }
        } else {
            interval.reset();
            this.intervalPool.add(interval);
        }
    }

    public Collection<Object> getGeneratedValues() {
        Debug.devAssert(mode != IntervalsMode.FAST,
                "No point in trying to get the generated values back out if nothing has been saved");

        return saved.stream().map(Interval::getGeneratedValue).collect(Collectors.toList());
    }
}
