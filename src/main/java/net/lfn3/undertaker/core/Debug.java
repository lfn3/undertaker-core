package net.lfn3.undertaker.core;

public class Debug {
    static boolean userDebug = true; //TODO: Read from... env var?
    static boolean devDebug = true; //TODO: Read from... env var?

    public static void userAssert(boolean condition, String message) {
        assert !userDebug || condition : message;
    }

    public static void devAssert(boolean condition, String message) {
        assert !devDebug || condition : message;
    }
}
