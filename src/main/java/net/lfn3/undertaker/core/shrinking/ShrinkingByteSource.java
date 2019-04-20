package net.lfn3.undertaker.core.shrinking;

import net.lfn3.undertaker.core.source.ByteSource;

public interface ShrinkingByteSource extends ByteSource {
    void revertShrink();
}
