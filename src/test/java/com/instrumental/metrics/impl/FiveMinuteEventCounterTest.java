package com.instrumental.metrics.impl;

import com.instrumental.metrics.counter.EventCounter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;

public class FiveMinuteEventCounterTest {

    private static EventCounter fiveMinuteCounter;

    @Before
    public void setup() {
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
    public void verifyZeroCountForOutdatedEvents() throws Exception {
        Instant startTimestamp = Instant.now().minusSeconds(10);

        for (int i = 0; i < 1000000; i++) {
            fiveMinuteCounter.incrementEventCount(startTimestamp);
        }
        //Any outdated events will be dropped with no side effect.
        assertEquals(0, fiveMinuteCounter.getEventCount(Duration.ofSeconds(10)));
    }

    //@Ignore(value = "Ignored to have a satisfactory build time for the artifact.")
    @Test
    public void oneSecondOutsideWindowCheck() throws Exception {
        fiveMinuteCounter.incrementEventCount(Instant.now());
        //Sleep 5 minutes and 1 sec, then assert on the event count over the 5 minute window. Should be 0.
        Thread.sleep(1000 + (5 * 60 * 1000));

        assertEquals(0, fiveMinuteCounter.getEventCount(Duration.ofSeconds(1)));
        assertEquals(0, fiveMinuteCounter.getEventCount(Duration.ofMinutes(5)));
    }

    @Test
    public void bulkInsertNoIntermediateSleep() throws Exception {
        Instant startTimestamp = Instant.now();

        for(int i = 0; i < 5000000; i++) {
            fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(1));
        }
        //Allow events to be accounted for.
        Thread.sleep(1000);
        //Should be 5000000.
        assertEquals(5000000, fiveMinuteCounter.getEventCount(Duration.ofMinutes(1)));
    }

    @Test
    public void validateLastFewInserts() throws Exception {
        Instant startTimestamp = Instant.now();

        fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(3));
        fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(2));
        fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(1));
        //Allow the time to lapse to the next three seconds.
        Thread.sleep(3 * 1000);
        //Should be 3.
        assertEquals(3, fiveMinuteCounter.getEventCount(Duration.ofSeconds(3)));
        assertEquals(3, fiveMinuteCounter.getEventCount(Duration.ofMinutes(1)));
    }

    @Ignore(value = "Ignored to have a satisfactory build time for the artifact.")
    @Test
    public void fullSlotCountCheck() throws Exception {
        Instant startTimestamp = Instant.now();
        System.out.println("Starting at: " + startTimestamp.toString());

        for(int i = 0; i < 300; i++) {
            fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(i));
        }
        //Not sure what was the issue here but the for loop based sleep simply did not work on occasion.
        Thread.sleep(5 * 60 * 1000);
        //Should be 298 or 299 based on the timestamp when the count() API is invoked.
        int result = fiveMinuteCounter.getEventCount(Duration.ofMinutes(5));
        System.out.println("5 minute count: " + result);
        assertTrue(result >= 298 && result <= 300 );
        //Should be 4 or 3 based on the timestamp when the count() API is invoked.
        result = fiveMinuteCounter.getEventCount(Duration.ofSeconds(5));
        System.out.println("5 second count: " + result);
        assertTrue(result >= 3 && result <= 5);
    }

    @Ignore(value = "Ignored to have a satisfactory build time for the artifact.")
    @Test
    public void almostFullSlotCountCheck() throws Exception {
        Instant startTimestamp = Instant.now();
        System.out.println("Starting at: " + startTimestamp.toString());

        for(int i = 0; i < 298; i++) {
            fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(i));
            Thread.sleep(1000); //Move to next slot (second).
        }
        //Should be 298.
        assertEquals(298, fiveMinuteCounter.getEventCount(Duration.ofMinutes(5)));
        //Should be 3.
        assertEquals(3, fiveMinuteCounter.getEventCount(Duration.ofSeconds(5)));
    }

    @Test
    public void boundaryCountCheck() throws Exception {
        Instant startTimestamp = Instant.now();

        for(int i = 0; i < 10; i++) {
            fiveMinuteCounter.incrementEventCount(startTimestamp.plusSeconds(i));
            Thread.sleep(1000); //Move to next slot (second).
        }

        assertEquals(9, fiveMinuteCounter.getEventCount(Duration.ofSeconds(10)));
        assertEquals(10, fiveMinuteCounter.getEventCount(Duration.ofSeconds(11)));
    }

    @Ignore(value = "Ignored to have a satisfactory build time for the artifact.")
    @Test
    public void bulkInsertCountCheck() throws Exception {
        Instant startInstant = Instant.now();

        for (int i = 0; i < 1000000; i++) {
            fiveMinuteCounter.incrementEventCount(startInstant);
        }

        for (int i = 0; i < 2000000; i++) {
            fiveMinuteCounter.incrementEventCount(startInstant.plusSeconds(1));
        }

        for(int i = 0; i < 3000000; i++) {
            fiveMinuteCounter.incrementEventCount(startInstant.plusSeconds(2));
        }

        Thread.sleep(2 * 60 * 1000);
        //Expect full count (6000000)
        assertEquals(6000000, fiveMinuteCounter.getEventCount(Duration.ofMinutes(2).plusSeconds(30)));
    }
}