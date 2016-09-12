package com.hcse.app;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.hcse.app.util.Counter;
import com.hcse.app.util.CounterManager;
import com.hcse.app.util.CounterTimer;
import com.hcse.protocol.BaseRequest;

@SuppressWarnings("rawtypes")
public class BenchmarkClientRunner implements ClientRunner {

    protected final Logger logger = Logger.getLogger(BenchmarkClientRunner.class);

    private ClientEvents handler;
    private CommonClient client;
    private BenchmarkConf conf;

    public static class BenchmarkClientContext extends RunnerContext {
        public long start;
    }

    public BenchmarkClientRunner() {
        handler = new ClientEvents<BenchmarkClientContext>() {

            @Override
            public void onLanch(BenchmarkClientContext ctx) {
                launchCounter.increment();
                ctx.start = System.nanoTime();
            }

            @Override
            public void onCompleted(BenchmarkClientContext ctx, boolean empty) {
                if (empty) {
                    emptyCounter.increment();
                }

                long now = System.nanoTime();
                successCounter.increment(now - ctx.start);
            }

            @Override
            public void onFailed(BenchmarkClientContext ctx) {
                long now = System.nanoTime();
                failedCounter.increment(now - ctx.start);
            }

            @Override
            public void onConnectTimeout(BenchmarkClientContext ctx) {
                connectTimeoutCounter.increment();
            }

            @Override
            public void onRequestTimeout(BenchmarkClientContext ctx) {
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
    public void run(BaseClientConf _conf, BaseClient _client) {
        client = (CommonClient) _client;
        conf = (BenchmarkConf) _conf;

        client.setClientEventHandler(handler);

        long stopTimeMillis = System.currentTimeMillis() + conf.testTime;

        stopTime = new Date(stopTimeMillis);

        qps = conf.qps;
        dumpInterval = conf.dumpInterval;

        executor = Executors.newFixedThreadPool(conf.threadNumber);
        dumpThread.start();

        initialThread.start();

        while (client.isRunning()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {

            }
        }
    }

    public void stop() {
        logger.info("press test stop.");

        dumpThread.interrupt();
        initialThread.interrupt();

        executor.shutdownNow();
    }

    private Date stopTime;
    private int qps;
    private int dumpInterval;
    private ExecutorService executor = null;

    private void dumpInfo() {
        while (client.isRunning()) {
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
        client.stop();
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
        RunnerContext ctx;
        BaseRequest req;

        public TestTask(BaseRequest req) {
            ctx = new BenchmarkClientContext();
            this.req = req;
        }

        @Override
        public void run() {
            client.handleRequest(ctx, req);
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

        RequestQueue<BaseRequest> queue = client.getRequestQueue();

        while (client.isRunning()) {
            long _queueLength = queueLength();

            if (_queueLength >= qps * 1.5) {
                sleep = 1000;
            } else {
                start = System.currentTimeMillis();

                for (int i = 0; i < qps; i++) {

                    executor.execute(new TestTask(queue.getRequest()));
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
