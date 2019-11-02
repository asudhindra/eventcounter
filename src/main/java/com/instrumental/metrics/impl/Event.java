package com.instrumental.metrics.impl;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the occurrence of a certain event, it's latest occurrence recorded in time using an {@code Instant} and the
 * frequency of these events recorded using an {@code AtomicInteger}.
 */
public class Event {

    private Instant eventInstant;

    private AtomicInteger count;

    public Event(Instant eventInstant) {
        this.eventInstant = eventInstant;
        this.count = new AtomicInteger(0);
    }

    /**
     * Given the current {@code Instant}, either increment or reset the frequency of the event based on whether the
     * event is for the current second or not.
     * @param currentInstant {@code Instant} object representing the current point in time the event occurred.
     */
    public void incrementOrResetCount(Instant currentInstant) {
        if (currentInstant.getEpochSecond() == eventInstant.getEpochSecond()) {
            this.count.incrementAndGet();
            return;
        }
        this.eventInstant = currentInstant;
        this.count.set(1);
    }

    /**
     * Fetches the number of occurrences (or frequency) of an event captured for the current point in time.
     * @return Frequency of the event for it's current {@code Instant}.
     */
    public int getCount() {
        return this.count.get();
    }

    /**
     * Fetches the {@code Instant} representing the timestamp associated with this event.
     * @return {@code Instant} representing the point in time this event was recorded.
     */
    public Instant getEventInstant() {
        return this.eventInstant;
    }
}
