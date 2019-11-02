package com.instrumental.metrics.impl;

import com.instrumental.metrics.counter.EventCounter;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkedListEventCounterImpl implements EventCounter {

    private Instant startTimestamp;

    private long result;

    private final LinkedList<Instant> events;

    private final int windowSizeInMinutes;

    public LinkedListEventCounterImpl(int windowSizeInMinutes) {
        this.windowSizeInMinutes = windowSizeInMinutes;
        startTimestamp = Instant.now();
        result = 0;
        events = new LinkedList<>();
    }

    @Override
    public void incrementEventCount() {
        Instant currentInstant = Instant.now();
        //Append the entry to the end of the list
        events.addLast(currentInstant);
        //Purge any events older than the windowSize
        Iterator<Instant> eventIterator = events.listIterator();
        while (eventIterator.hasNext()) {
            Instant eventInstant = eventIterator.next();
            if( Duration.between(currentInstant, eventInstant).getSeconds() > windowSizeInMinutes * 60) {
                //Element needs to be remove
                eventIterator.remove();
            }
        }
    }

    @Override
    public int getEventCount(final Duration resultDuration) {
        return 0;
    }

    public long getRecentEventCount(final Duration resultDuration) {
        long resultDurationInMinutes = TimeUnit.MINUTES.toMinutes(resultDuration.getSeconds());
        Instant resultCutoff = Instant.now().minus(resultDuration);
        if(resultDuration.getSeconds() > windowSizeInMinutes * 60) {
            throw new IllegalArgumentException("Cannot fetch counts for events outside counter window. You requested ["
                + resultDurationInMinutes + "] minutes but are only collecting [" + windowSizeInMinutes + "] minutes.");
        }
        //We want the complete list of counts, so return the entire result set
        if(resultDurationInMinutes == windowSizeInMinutes) {
            return events.size();
        }

        AtomicInteger result = new AtomicInteger(0);
        synchronized (events) {
            Iterator<Instant> eventIterator = events.listIterator();
            while (eventIterator.hasNext()) {
                Instant eventInstant = eventIterator.next();
                if (!eventInstant.isBefore(resultCutoff)) {
                    result.incrementAndGet();
                }
            }
        }
        return result.get();
    }
}
