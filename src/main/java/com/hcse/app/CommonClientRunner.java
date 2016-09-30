package com.hcse.app;

import java.util.List;

import org.apache.log4j.Logger;

import com.hcse.app.BenchmarkClientRunner.BenchmarkClientContext;
import com.hcse.app.util.Counter;
import com.hcse.app.util.CounterManager;
import com.hcse.app.util.CounterTimer;
import com.hcse.protocol.BaseRequest;

public class CommonClientRunner implements ClientRunner {
    protected final Logger logger = Logger.getLogger(CommonClientRunner.class);

    private int dumpInterval = 20 * 1000;
    private CommonClient client;
    private ClientEvents handler;

    private Counter launchCounter = new Counter("launch");
    private CounterTimer successCounter = new CounterTimer("success");
    private CounterTimer failedCounter = new CounterTimer("failed");

    private CounterTimer emptyCounter = new CounterTimer("empty");

    private Counter connectTimeoutCounter = new Counter("connectTimeout");
    private Counter requestTimeoutCounter = new Counter("requestTimeout");

    public CommonClientRunner() {
        handler = new ClientEvents<BenchmarkClientContext>() {

            @Override
            public void onLanch(BenchmarkClientContext ctx) {
                launchCounter.increment();
                ctx.start = System.currentTimeMillis();
            }

            @Override
            public void onCompleted(BenchmarkClientContext ctx, boolean empty) {
                long now = System.currentTimeMillis();

                if (empty) {
                    emptyCounter.increment();
                }

                long usage = now - ctx.start;
                successCounter.increment(usage);

                if (logger.isDebugEnabled()) {
                    logger.debug("common client completed. usage:" + usage);
                }
            }

            @Override
            public void onFailed(BenchmarkClientContext ctx) {
                long now = System.currentTimeMillis();
                long usage = now - ctx.start;
                failedCounter.increment(usage);

                if (logger.isDebugEnabled()) {
                    logger.debug("common client failed. usage:" + usage);
                }
            }

            @Override
            public void onConnectTimeout(BenchmarkClientContext ctx) {
                connectTimeoutCounter.increment();
                if (logger.isDebugEnabled()) {
                    logger.debug("common client conntect timeout.");
                }
            }

            @Override
            public void onRequestTimeout(BenchmarkClientContext ctx) {
                requestTimeoutCounter.increment();
                if (logger.isDebugEnabled()) {
                    logger.debug("common client request timeout.");
                }
            }
        };
    }

    public int getDumpInterval() {
        return dumpInterval;
    }

    public void setDumpInterval(int dumpInterval) {
        this.dumpInterval = dumpInterval;
    }

    private void dump() {
        List<String> dumpContent = CounterManager.getInstance().dump();

        for (String l : dumpContent) {
            logger.info(l);
        }
    }

    private void dumpInfo() {
        while (client.isRunning()) {
            try {
                Thread.sleep(dumpInterval);
            } catch (InterruptedException e) {
                continue;
            }

            dump();
        }
    }

    private Thread dumpThread = new Thread(new Runnable() {
        @Override
        public void run() {
            dumpInfo();
            logger.info("dump thread stop.");
        }
    });

    @Override
    public void run(BaseClientConf conf, BaseClient c) {
        client = (CommonClient) c;

        dumpThread.start();
        client.setClientEventHandler(handler);

        BenchmarkClientContext ctx = new BenchmarkClientContext();

        while (client.isRunning()) {
            BaseRequest request = client.getRequestQueue().getRequest();

            if (request == null) {
                break;
            }

            client.handleRequest(ctx, request);
        }

        client.stop();
        try {
            dumpThread.join();
        } catch (InterruptedException e) {

            // e.printStackTrace();
        }

        dump();
    }
}
