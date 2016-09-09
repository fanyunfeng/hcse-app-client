package com.hcse.app.d2p;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.hcse.app.BaseClientConf;
import com.hcse.app.d2.ClientEvents;
import com.hcse.app.d2.D2Client;
import com.hcse.app.util.Counter;
import com.hcse.app.util.CounterManager;
import com.hcse.app.util.CounterTimer;
import com.hcse.protocol.d2.message.D2RequestMessage;

public class BenchmarkClient extends D2Client {
    protected final Logger logger = Logger.getLogger(BenchmarkClient.class);

    BenchmarkClient() {
        handler = new ClientEvents() {
            long start;

            @Override
            public void onLanch() {
                launchCounter.increment();
                start = System.currentTimeMillis();
            }

            @Override
            public void onCompleted(boolean empty) {
                if (empty) {
                    emptyCounter.increment();
                }

                long now = System.currentTimeMillis();
                successCounter.increment(now - start);
            }

            @Override
            public void onFailed() {
                long now = System.currentTimeMillis();
                failedCounter.increment(now - start);
            }

            @Override
            public void onConnectTimeout() {
                connectTimeoutCounter.increment();
            }

            @Override
            public void onRequestTimeout() {
                requestTimeoutCounter.increment();
            }
        };
    }

    private Counter initialCounter = new Counter("initial");
    private Counter launchCounter = new Counter("launch");
    private CounterTimer successCounter = new CounterTimer("success");
    private CounterTimer failedCounter = new CounterTimer("failed");

    private CounterTimer emptyCounter = new CounterTimer("empty");

    private Counter connectTimeoutCounter = new Counter("connectTimeout");
    private Counter requestTimeoutCounter = new Counter("requestTimeout");

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void run(BaseClientConf bconf) {
        BenchmarkConf conf = (BenchmarkConf) bconf;

        long stopTimeMillis = System.currentTimeMillis() + conf.testTime;

        stopTime = new Date(stopTimeMillis);

        qps = conf.qps;
        dumpInterval = conf.dumpInterval;

        executor = Executors.newFixedThreadPool(conf.threadNumber);
        dumpThread.start();
    }

    public void stop() {
        logger.info("press test stop.");

        dumpThread.interrupt();
        initialThread.interrupt();

        executor.shutdownNow();

        super.stop();
    }

    private Date stopTime;
    private int qps;
    private int dumpInterval;
    private ExecutorService executor = null;

    private void dumpInfo() {
        while (running.get()) {
            try {
                Thread.sleep(dumpInterval);
            } catch (InterruptedException e) {
                continue;
            }

            List<String> dumpContent = CounterManager.getInstance().dump();

            long _queueLength = queueLength();

            if (_queueLength > 0) {
                logger.info("" + _queueLength + " request queued.");
            }

            for (String l : dumpContent) {
                logger.info(l);
            }

            Date now = new Date();

            if (now.after(stopTime)) {
                break;
            }
        }

        stop();
    }

    private long queueLength() {
        return initialCounter.getCurrentValue() - launchCounter.getCurrentValue();
    }

    private Thread dumpThread = new Thread(new Runnable() {
        @Override
        public void run() {
            dumpInfo();
            logger.info("dump thread stop.");
        }
    });

    class TestTask implements Runnable {
        D2RequestMessage req;

        public TestTask(D2RequestMessage req) {
            this.req = req;
        }

        @Override
        public void run() {
            handleRequest(req);
        }
    }

    private Thread initialThread = new Thread(new Runnable() {
        @Override
        public void run() {
            initialRequest();
            logger.info("initial thread stop.");
        }
    });

    void initialRequest() {
        long start;
        long end;
        long used;

        long sleep;

        while (running.get()) {
            long _queueLength = queueLength();

            if (_queueLength >= qps * 6) {
                sleep = 1000;
            } else {
                start = System.currentTimeMillis();

                for (int i = 0; i < qps; i++) {

                    executor.execute(new TestTask(requestQueue.getRequest()));
                    initialCounter.increment();
                }

                end = System.currentTimeMillis();

                used = end - start;

                sleep = 1000 - used;
            }

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {

                }
            } else if (sleep < 0) {
                logger.error("cant initial request " + qps + " /second.");
            }
        }
    }

}
