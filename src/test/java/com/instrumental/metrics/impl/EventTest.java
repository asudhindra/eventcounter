package com.instrumental.metrics.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventTest {

    private static Event sampleEvent;
    private static Instant eventInstant = Instant.now();

    @BeforeClass
    public static void setup() {
        sampleEvent = new Event(eventInstant);
    }

    @Test
    public void validValuesOnInit() {
        assertNotNull(sampleEvent);
    }

    @Test
    public void validateIncrementOrResetCount() {
        //Check 1: Validate same second since epoch behavior
        Instant currentInstant = eventInstant;
        sampleEvent.incrementOrResetCount(currentInstant);
        assertEquals(1, sampleEvent.getCount());
        assertEquals(eventInstant, sampleEvent.getEventInstant());

        //Check 2: Validate newer event arrival
        currentInstant = eventInstant.plusSeconds(2);
        sampleEvent.incrementOrResetCount(currentInstant);
        assertEquals(1, sampleEvent.getCount());
        assertEquals(currentInstant, sampleEvent.getEventInstant());
    }

}