package com.quentinjian.receiptlottery;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkPrizeTest() throws IOException {
        assertEquals(PrizeType.FIRST, CheckPrize.determinePrice("26649927"));
        assertEquals(PrizeType.SPECIAL, CheckPrize.determinePrice("13965913"));
        assertEquals(PrizeType.SIXTH, CheckPrize.determinePrice("12345822"));
    }
}