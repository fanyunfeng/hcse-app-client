package com.hcse.d6.app;

import java.util.concurrent.atomic.AtomicLong;

public class Counter {
    private String name;

    private long last = 0;
    private long current = 0;

    private AtomicLong counter = new AtomicLong();

    public Counter(String name) {
        this.name = name;

        CounterManager.getInstance().register(this);
    }

    public void increment() {
        counter.incrementAndGet();
    }

    public void save() {
        last = current;

        current = counter.get();
    }

    public long diff() {
        return current - last;
    }

    public long get() {
        return current;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
