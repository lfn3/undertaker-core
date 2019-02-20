package net.lfn3.undertaker.core.intervals;

public class Interval {
    private IntervalType type;
    private Object generatedValue;
    private boolean markChildrenAsSnippable;
    private boolean snippable;

    void populate(IntervalType type, boolean markChildrenAsSnippable, boolean snippable) {
        assert this.type == null : "Interval was not reset before attempting reuse.";
        assert this.generatedValue == null : "Interval was not reset before attempting reuse.";
        assert !this.snippable : "Interval was not reset before attempting reuse.";
        assert !this.markChildrenAsSnippable : "Interval was not reset before attempting reuse.";

        this.snippable = snippable;
        this.markChildrenAsSnippable = markChildrenAsSnippable;
        this.type = type;
    }

    void retain(Object generatedValue)
    {
        this.generatedValue = generatedValue;
    }

    void reset()
    {
        generatedValue = null;
        type = null;
        snippable = false;
        markChildrenAsSnippable = false;
    }

    // region getters
    IntervalType getType() {
        return this.type;
    }

    Object getGeneratedValue() {
        return this.generatedValue;
    }

    boolean isSnippable() {
        return this.snippable;
    }

    boolean shouldMarkChildrenAsSnippable() {
        return markChildrenAsSnippable;
    }
    // endregion
}
