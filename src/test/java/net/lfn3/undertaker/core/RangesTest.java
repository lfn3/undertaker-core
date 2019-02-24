package net.lfn3.undertaker.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangesTest {
    @Test
    public void isIn() {
        Ranges r1 = Ranges.fromFlatArray(new byte[]{ 1, 2, 3, 4 }, 2);
        assertTrue(r1.isIn(new byte[]{ 2, 3 }));
        assertTrue(r1.isIn(new byte[]{ 2, -1 })); //Middle of range, shouldn't care about later values.
        assertTrue(r1.isIn(new byte[]{ 1, 5 })); //Lower bound for first value means we don't care about upper bound anymore.
        assertTrue(r1.isIn(new byte[]{ 3, 0 })); //And vice versa
        assertTrue(r1.isIn(new byte[]{ 1 })); //In range at the start.
        assertFalse(r1.isIn(new byte[]{ 2, 3, 5 }));

        Ranges r2 = Ranges.fromFlatArray(new byte[]{ 1, 2, 3, 4 }, 2);
        assertFalse(r2.isIn(new byte[]{ 4, 3 }));

        Ranges r3 = Ranges.fromFlatArray(new byte[]{ -127, 6, -1, 10 }, 2);
        assertTrue(r3.isIn(new byte[]{ -55, 8 }));

        Ranges r4 = Ranges.fromFlatArray(new byte[]{ -127, 6, -1, 10,
                                                     1, 3 , 6, 3},
                2);
        assertTrue(r4.isIn(new byte[]{ 2, 8 }));

        Ranges r5 = Ranges.fromFlatArray(new byte[]{ 1, 2, 3,
                                                     4, 5, 6 }, 3);
        assertTrue(r5.isIn(new byte[]{ 1, 5, -1 })); //Bouncing off the lower and upper is equivalent to being in the middle.
        assertTrue(r5.isIn(new byte[]{ 4, 2, -1 })); //And vice versa
    }
}