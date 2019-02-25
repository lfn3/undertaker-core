package net.lfn3.undertaker.core.intervals;

import java.util.EnumSet;

public enum IntervalFlag {
    SNIPPABLE,
    SNIPPABLE_CHILDREN,
    UNIQUE,
    UNIQUE_CHILDREN;

    public static EnumSet<IntervalFlag> NONE = EnumSet.noneOf(IntervalFlag.class);
}
