package com.instrumental.metrics;

import com.instrumental.metrics.counter.EventCounter;
import com.instrumental.metrics.impl.FiveMinuteEventCounter;

import java.time.Duration;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Slot based implementation" );

        EventCounter counter = new FiveMinuteEventCounter();

        for (int i = 0; i < 1000000; i++) {
            counter.incrementEventCount();
        }

        Thread.sleep(1 * 60 * 1000);
        for (int i = 0; i < 1000000; i++) {
            counter.incrementEventCount();
        }

        for (int i = 0; i < 100000; i++) {
            counter.incrementEventCount();
        }

        counter.incrementEventCount();
        counter.incrementEventCount();
        counter.incrementEventCount();
        Thread.sleep(1 * 60 * 1000);

        System.out.println(counter.getEventCount(Duration.ofMinutes(2)));
    }
}
