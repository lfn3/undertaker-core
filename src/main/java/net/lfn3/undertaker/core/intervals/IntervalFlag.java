package net.lfn3.undertaker.core.intervals;

import java.util.EnumSet;

public enum IntervalFlag {
    //Hints for byte sources. Or range manglers. Not really sure where this lives, yet.
    UNIQUE,
    UNIQUE_CHILDREN,
    //Hints for collection shrinkers
    COLLECTION,
    COLLECTION_SIZE,
    SNIPPABLE,
    SNIPPABLE_CHILDREN,

    //If this interval should have children or not
    COMPOSITE,
    VALUE;

    public static EnumSet<IntervalFlag> NONE = EnumSet.noneOf(IntervalFlag.class);
}
