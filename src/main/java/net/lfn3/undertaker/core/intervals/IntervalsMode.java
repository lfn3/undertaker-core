package net.lfn3.undertaker.core.intervals;

public enum IntervalsMode {
    FAST, // We're just trying to generate test cases as quickly as possible
    DISPLAY, // Hang onto enough information to tell the user or test what values were generated
    SHRINK, // Hold onto intervals that we can shrink.
    DEBUG
}
