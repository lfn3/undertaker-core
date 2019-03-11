package net.lfn3.undertaker.core;

import java.util.function.Supplier;

public class Debug {
    private final static boolean USER_DEBUG = true; //TODO: Read from... env var?
    private final static boolean DEV_DEBUG = true; //TODO: Read from... env var?

    public static void userAssert(Supplier<Boolean> condition, String message) {
        if (USER_DEBUG) {
            assert condition.get() : message;
        }
    }

    public static void userAssert(boolean condition, String message) {
        if (USER_DEBUG) {
            assert condition : message;
        }
    }

    public static void devAssert(Supplier<Boolean> condition, String message) {
        if (DEV_DEBUG) {
            assert condition.get() : message;
        }
    }

    public static void devAssert(boolean condition, String message) {
        if (DEV_DEBUG) {
            assert condition : message;
        }
    }
}
