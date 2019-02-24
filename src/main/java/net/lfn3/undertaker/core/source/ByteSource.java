package net.lfn3.undertaker.core.source;

import net.lfn3.undertaker.core.Ranges;

import java.nio.ByteBuffer;

public interface ByteSource {
    ByteBuffer nextBytes(Ranges ranges);
}
