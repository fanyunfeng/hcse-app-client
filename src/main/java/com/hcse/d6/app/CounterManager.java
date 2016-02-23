package com.hcse.d6.app;

import java.util.ArrayList;

public class CounterManager {
    private long start;

    private ArrayList<Counter> counters = new ArrayList<Counter>();

    static class CounterManagerInstance {
        static CounterManager instance = new CounterManager();
    };

    public static CounterManager getInstance() {
        return CounterManagerInstance.instance;
    }

    public void register(Counter c) {
        counters.add(c);
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public String dump() {

        for (Counter c : counters) {
            c.save();
        }

        double ps;
        long value;
        long second = System.currentTimeMillis() - start;

        second = second / 1000;

        StringBuilder sb = new StringBuilder();

        sb.append("current");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            sb.append(c.get());
        }

        sb.append('\n');

        sb.append("diff");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            sb.append(c.diff());
        }

        sb.append('\n');

        sb.append("second");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            value = c.get();
            if (value == 0) {
                sb.append("0");
            } else {
                ps = (double) value / second;
                sb.append(ps);
            }
        }

        sb.append('\n');

        sb.append("diffsecond");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            value = c.diff();
            if (value == 0) {
                sb.append("0");
            } else {
                ps = (double) value / second;
                sb.append(ps);
            }
        }

        sb.append('\n');

        sb.append("speed");
        for (Counter c : counters) {
            if (c instanceof CounterTimer) {
                CounterTimer t = (CounterTimer) c;
                sb.append(" ");
                sb.append(c.getName());
                sb.append(":");
                sb.append(t.speed());
            }
        }

        sb.append('\n');

        sb.append("averageSpeed");
        for (Counter c : counters) {
            if (c instanceof CounterTimer) {
                CounterTimer t = (CounterTimer) c;
                sb.append(" ");
                sb.append(c.getName());
                sb.append(":");
                sb.append(t.averageSpeed());
            }
        }

        sb.append('\n');

        return sb.toString();
    }
}
