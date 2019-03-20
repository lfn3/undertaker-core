package net.lfn3.undertaker.core;

import java.util.function.Supplier;

public class UserDebug {
    public static void userAssert(Supplier<Boolean> condition, String message) {
        assert condition.get() : message;
    }

    public static void userAssert(boolean condition, String message) {
        assert condition : message;
    }
}
