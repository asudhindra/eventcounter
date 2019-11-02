package com.instrumental.metrics.counter;

import java.time.Duration;

/**
 * Simple counter that allows collection of occurrences of a certain event. Implementations of this interface will allow
 * you to instantiate, increment and collect counts of a singular event over a specified window of time.
 */
public interface EventCounter {

    /**
     * Records the occurrence of a single event on the counter. The time at which the event occurred is recorded
     * internally, so the client only needs to record the occurrence of the event being recorded.
     */
    void incrementEventCount();

  /**
   * Retrieves the frequency of the event being recorded over a user specified amount of time,
   * ending in the current time.
   * @param desiredDuration Duration over which the frequency of the event needs to be computed, for some specific
   *                        amount of time ending at the present time.
   * @return Number of events recorded over the specified duration.
   * @throws IllegalArgumentException if the {@code Duration} object exceeds the window of time being recorded for events.
   */
  int getEventCount(Duration desiredDuration) throws IllegalArgumentException;
}
