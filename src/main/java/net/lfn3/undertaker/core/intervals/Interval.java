package net.lfn3.undertaker.core.intervals;

import net.lfn3.undertaker.core.DevDebug;

import java.util.EnumSet;

public class Interval {
    static final Interval NONE = new Interval();

    EnumSet<IntervalFlag> flags;

    private Object generatedValue;

    void populate(final EnumSet<IntervalFlag> flags) {
        assertNotNone();
        assertUnpopulated();
        DevDebug.devAssert(flags != null, "Flags may not be null.");

        this.flags = flags;
    }

    void retain(Object generatedValue) {
        assertPopulated();

        this.generatedValue = generatedValue;
    }

    void reset() {
        assertPopulated();

        generatedValue = null;
        flags = null;
    }

    //region assertions
    private void assertUnpopulated() {
        DevDebug.devAssert(this.generatedValue == null, "Interval was not reset before attempting reuse.");
        DevDebug.devAssert(this.flags == null, "Interval was not reset before attempting reuse.");
    }

    private void assertNotNone()
    {
        DevDebug.devAssert(this != NONE, "You're trying to actually use the NONE interval");
    }

    private void assertPopulated()
    {
        assertNotNone();
        DevDebug.devAssert(this.flags != null, "Interval hasn't been populated yet");
    }
    // endregion assertions

    // region getters

    Object getGeneratedValue() {
        assertPopulated();

        return this.generatedValue;
    }

    EnumSet<IntervalFlag> getFlags() {
        assertPopulated();

        return this.flags;
    }

    boolean hasFlag(IntervalFlag flag) {
        assertPopulated();

        return this.flags.contains(flag);
    }
    // endregion
}
