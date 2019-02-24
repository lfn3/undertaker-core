package net.lfn3.undertaker.core;

import net.lfn3.undertaker.core.source.WrappedRandomByteSource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SourceRule implements TestRule {
    private final WrappedRandomByteSource source = new WrappedRandomByteSource();

    @Override
    public Statement apply(Statement base, Description description) {
        return base;
    }

    public byte[] nextBytes(final int length) {
        final byte[] result = new byte[length];
        source.fill(result);

        return result;
    }

    public byte[] nextBytes(final byte[] lowerBound, final byte[] upperBound, final byte[]... moreRanges) {
        Ranges r = Ranges.fromArrays(lowerBound, upperBound, moreRanges);
        return source.nextBytes(r).array();
    }
}
