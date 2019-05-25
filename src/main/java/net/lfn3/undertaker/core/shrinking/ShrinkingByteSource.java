package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.Range;
import net.lfn3.undertaker.core.source.ByteSource;

public interface ShrinkingByteSource extends ByteSource {
    void revertShrink();

    /**
     * Use this to determine if a shrinker is done.
     * Shrinkers are allowed to throw if this would return true and any overload of
     * {@link ByteSource#nextBytes(Range)} is called.
     *
     * @return boolean indicating if this shrinker has run out of ways to further reduce the bytes it's operating on.
     */
    boolean isExhausted();

    default void confirmShrink() {
        reset();
        next();
    }

    default void rejectShrink() {
        revertShrink();
        next();
    }
}
