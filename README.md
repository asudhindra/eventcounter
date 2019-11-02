# eventcounter

## What is this?
`eventcounter` is a simple library that will allow you to count the frequency of a certain event
over the last five minutes. It currently only exposes two APIs:
* `incrementEventCount()`: This API increments the count of an event for the current timestamp (recorded as an instance 
of type `Instant`).
* `getEventCount(Duration)`: This API fetches the frequency of events recorded over a finite `Duration` ending at the 
present `Instant`. Should the client try to request data for a window exceeding what is the maximum size (5 minutes), 
this API throws an `IllegalArgumentException`.

## How do I use this library?
This project is specified to be packaged as a `jar` file, so running 

`mvn clean package` OR
`mvn clean install`

should build a jar for you (and install it to your local Maven repository in case of `install`). There are a couple of 
unit tests (JUnit 4.13) that check very trivial conditions for the library, more on that below.

## Testing
As mentioned, the current unit tests for the library are not indicative of the testing done, and are only basic sanity 
checks. Here are some (not all) additional test cases that were checked but not added to the unit test suite to avoid 
build delays and timeouts.

* Case 1: No inserts during the last minute, query for last minute

```$java
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

//Returns 0, since no new events were recorded over the last minute.
System.out.println(counter.getEventCount(Duration.ofMinutes(1)));
```

* Case 2: One insert, now expired (posted over 5 minutes ago), query for 5 minutes. Should return 0.
 
```$java
EventCounter counter = new FiveMinuteEventCounter();

for (int i = 0; i < 1000000; i++) {
    counter.incrementEventCount();
}

Thread.sleep(5 * 60 * 1000);

//Returns 0, since the last insert was over 5 minutes ago.
System.out.println(counter.getEventCount(Duration.ofMinutes(5)));
```

* Case 3: Multiple, large volume (1M+) inserts, returns data correctly.
```$java
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

//Returns 1100003 since 1,1M events were available over the last 2 minutes.
System.out.println(counter.getEventCount(Duration.ofMinutes(2)));
```
