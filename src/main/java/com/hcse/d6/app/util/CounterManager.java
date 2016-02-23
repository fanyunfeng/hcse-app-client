package com.hcse.d6.app.util;

import java.util.ArrayList;
import java.util.List;

public class CounterManager {
    private long start = System.currentTimeMillis();
    private long second = 0;

    private String toString(double v) {
        if (Double.isNaN(v)) {
            return "0";
        }

        return String.format("%.3f", v);
    }

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

    public String dumpCurrent() {
        StringBuilder sb = new StringBuilder();

        sb.append("total");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            sb.append(c.get());
        }

        return sb.toString();
    }

    public String dumpDiff() {
        StringBuilder sb = new StringBuilder();

        sb.append("increment");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            sb.append(c.diff());
        }

        return sb.toString();
    }

    public String dumpPerSecond() {
        double ps;
        long value;
        StringBuilder sb = new StringBuilder();

        sb.append("perSecond[c/s]");

        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            value = c.diff();

            ps = (double) value / second;
            sb.append(toString(ps));
        }

        return sb.toString();
    }

    public String dumpAveragePerSecond() {
        double ps;
        long value;
        StringBuilder sb = new StringBuilder();

        sb.append("averagePerSecond[c/s]");
        for (Counter c : counters) {
            sb.append(" ");
            sb.append(c.getName());
            sb.append(":");
            value = c.get();

            ps = (double) value / second;
            sb.append(toString(ps));
        }

        return sb.toString();
    }

    public String dumpSpeed() {
        StringBuilder sb = new StringBuilder();

        sb.append("speed[ms/c]");

        for (Counter c : counters) {
            if (c instanceof CounterTimer) {
                CounterTimer t = (CounterTimer) c;
                sb.append(" ");
                sb.append(c.getName());
                sb.append(":");
                sb.append(toString(t.speed()));
            }
        }

        return sb.toString();
    }

    public String dumpAverageSpeed() {
        StringBuilder sb = new StringBuilder();

        sb.append("averageSpeed[ms/c]");
        for (Counter c : counters) {
            if (c instanceof CounterTimer) {
                CounterTimer t = (CounterTimer) c;
                sb.append(" ");
                sb.append(c.getName());
                sb.append(":");
                sb.append(toString(t.averageSpeed()));
            }
        }

        return sb.toString();
    }

    public List<String> dump() {
        for (Counter c : counters) {
            c.save();
        }

        second = System.currentTimeMillis() - start;

        second = second / 1000;

        ArrayList<String> content = new ArrayList<String>();

        content.add(dumpDiff());
        content.add(dumpCurrent());

        content.add(dumpPerSecond());
        content.add(dumpAveragePerSecond());

        content.add(dumpSpeed());
        content.add(dumpAverageSpeed());

        return content;
    }
}
