package com.hcse.d6.app;

import java.util.concurrent.atomic.AtomicLong;

public class CounterTimer extends Counter {
    private long last = 0;
    private long current = 0;

    private AtomicLong counter = new AtomicLong();

    public CounterTimer(String name) {
        super(name);
    }

    public void increment(long time) {
        super.increment();

        counter.addAndGet(time);
    }

    public void save() {
        super.save();

        last = current;

        current = counter.get();
    }

    public double averageSpeed() {
        long c = super.get();

        if (c == 0) {
            return 0;
        }

        return (double) current / c;
    }

    public double speed() {
        long c = super.diff();

        if (c == 0) {
            return 0;
        }

        return (double) (current - last) / c;
    }
}