package com.instrumental.metrics.impl;

import com.instrumental.metrics.counter.EventCounter;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class FiveMinuteEventCounter implements EventCounter {

    /*
     * Window size (in minutes) which defaults to five for this implementation.
     */
    private final int windowSizeInMinutes;

    /*
     * Second backed slots that accumulate counts over a given second.
     */
    private final Event[] secondSlots;

    /*
     * Start timestamp associated with the counter. Used to compute the index where occurrence of a new {@code Event}
     * would be recorded.
     */
    private Instant startInstant;

    public FiveMinuteEventCounter() {
        this.windowSizeInMinutes = 5;
        this.secondSlots = new Event[windowSizeInMinutes * 60];

        this.startInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Increments the occurrence of the event in the appropriate time slot. The client does not need to record the time
     * at which the event occurred as this is managed internally.
     */
    @Override
    public void incrementEventCount(Instant currentInstant) {
        currentInstant = currentInstant.truncatedTo(ChronoUnit.SECONDS);
        //Handles case where any old events were being inserted.
        if(currentInstant.isBefore(startInstant)) {
            return;
        }
        int secondSlotIndex = getIndex(currentInstant);
        if(secondSlots[secondSlotIndex] == null) {
            secondSlots[secondSlotIndex] = new Event(currentInstant);
        }
        secondSlots[secondSlotIndex].incrementOrResetCount(currentInstant);
    }

    /**
     * Returns the frequency of events accumulated over the {@code desiredDuration} which ends at the current instant.
     * The maximum value for which counts can be retrieved are the window size, which is the last five minutes.
     *
     * Any {@code Duration} value exceeding the window size will throw an {@code IllegalArgumentException} mentioning
     * the same.
     * @param desiredDuration Duration over which the frequency of the event needs to be computed, for some specific
     *                        amount of time ending at the present time.
     * @return Result accumulated over the {@desiredDuration}.
     */
    @Override
    public int getEventCount(Duration desiredDuration) throws IllegalArgumentException {
        Instant currentInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        if(desiredDuration.toMinutes() > windowSizeInMinutes) {
            throw new IllegalArgumentException("Cannot fetch counts for events outside counter window. You requested ["
                + desiredDuration.toMinutes() + "] minutes but are collecting [" + windowSizeInMinutes + "] minutes.");
        }

        int secondSlotIndex = getIndex(currentInstant);
        int numberOfSecondsToExamine = (int)desiredDuration.getSeconds();
        int slotsSize = windowSizeInMinutes * 60;

        int result = 0;
        // Having determined the number of seconds to be counted, compute the indices to be read from
        // the slot array.
        for (int i = 0; i < numberOfSecondsToExamine; i++) {
          int index = (secondSlotIndex - i + slotsSize) % slotsSize;
          Event event = secondSlots[index];
          if (event != null && Duration.between(event.getEventInstant(), currentInstant).getSeconds() == i) {
            result += event.getCount();
          }
        }
        return result;
    }

    /**
     * Computes a suitable index for accumulating the count for an {@code Event} instance or for
     * identifying where to start iterating when accumulating results for the {@code getEventCount()}
     * method.
     *
     * @param currentInstant Instance of {@code Instant} representing the current instant in time.
     * @return Index where the {@code Event} count will be accumulated for writes, slots array will be indexed for reads.
     */
    private int getIndex(final Instant currentInstant) {
        long num = currentInstant.getEpochSecond();
        return (int)(num % secondSlots.length);
    }
}
