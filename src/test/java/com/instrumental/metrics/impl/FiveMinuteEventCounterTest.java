package com.instrumental.metrics.impl;

import com.instrumental.metrics.counter.EventCounter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

/**
 * This series of unit tests is more to test different scenarios where we check for the accuracy of the frequency over
 * a the last few seconds. Avoiding {@code Thread.sleep()} based validations here, I will try to explain reasoning in
 * a README.
 */
public class FiveMinuteEventCounterTest {

    private static EventCounter fiveMinuteCounter;

    @BeforeClass
    public static void setup() {
        fiveMinuteCounter = new FiveMinuteEventCounter();
    }

    @Test
    public void sanityCheck() {
        assertNotNull(fiveMinuteCounter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyEventsCheck() {
        assertEquals(0, fiveMinuteCounter.getEventCount(Duration.ofMinutes(6)));
    }

    @Test
    public void checkThreeEvents() {
        for (int i = 0; i < 3; i++) {
            fiveMinuteCounter.incrementEventCount();
        }
        assertEquals(3, fiveMinuteCounter.getEventCount(Duration.ofMinutes(5)));
    }
}