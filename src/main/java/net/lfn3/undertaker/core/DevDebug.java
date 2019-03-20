package net.lfn3.undertaker.core;

import java.util.function.Supplier;

public class DevDebug {
    public static void devAssert(Supplier<Boolean> condition, String message) {
        assert condition.get() : message;
    }

    public static void devAssert(boolean condition, String message) {
        assert condition : message;
    }
}
