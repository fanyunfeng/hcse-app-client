package com.hcse.d6.app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import com.hcse.d6.app.util.Counter;
import com.hcse.d6.app.util.CounterManager;
import com.hcse.d6.app.util.CounterTimer;
import com.hcse.d6.protocol.factory.D6ResponseMessageFactory;
import com.hcse.d6.protocol.message.D6ResponseMessage;

public class Benchmark extends ClientBase {
    protected final Logger logger = Logger.getLogger(Benchmark.class);

    class TestTask implements Runnable {
        TestRequest req;

        public TestTask(ArrayList<TestRequestItem> req) {
            this.req = new TestRequest(req);
        }

        @Override
        public void run() {
            doRequest(req);
        }
    }

    private Counter initialCounter = new Counter("initial");
    private Counter launchCounter = new Counter("launch");
    private CounterTimer successCounter = new CounterTimer("success");
    private CounterTimer failedCounter = new CounterTimer("failed");
    private CounterTimer emptyCounter = new CounterTimer("empty");

    private ExecutorService executor = null;

    private int threadNumber = 10;

    private int qps = 10;

    private int docPerReq = 5;

    private int dumpInterval = 20 * 1000;

    private long timeTime = 1000 * 60;
    private Date stopTime;

    private ArrayList<TestRequestItem> requestInfo = new ArrayList<TestRequestItem>();

    private Random random = new Random(System.currentTimeMillis());

    private Thread initialThread = new Thread(new Runnable() {
        @Override
        public void run() {
            initialRequest();
        }
    });

    private Thread dumpTread = new Thread(new Runnable() {
        @Override
        public void run() {
            dumpInfo();
        }
    });

    private int sequence = 0;

    private int mode = 0;

    public ArrayList<TestRequestItem> genRandomRequest() {
        ArrayList<TestRequestItem> ret = new ArrayList<TestRequestItem>(docPerReq);
        HashSet<Integer> set = new HashSet<Integer>(docPerReq);

        int requestInfoSize = requestInfo.size();

        int count = 0;
        while (count < docPerReq) {
            int id = random.nextInt(requestInfoSize);

            if (set.contains(id)) {
                continue;
            }

            ret.add(requestInfo.get(id));

            count++;
        }

        return ret;
    }

    public ArrayList<TestRequestItem> genSequenceRequest() {
        ArrayList<TestRequestItem> ret = new ArrayList<TestRequestItem>(docPerReq);

        int requestInfoSize = requestInfo.size();

        int count = 0;
        while (count < docPerReq) {
            ret.add(requestInfo.get(sequence));

            sequence++;

            if (sequence >= requestInfoSize) {
                sequence = 0;
            }

            count++;
        }

        return ret;
    }

    private ArrayList<TestRequestItem> getRequest() {
        if (mode == 1) {
            return genSequenceRequest();
        } else {
            return genRandomRequest();
        }
    }

    private void addRequest(String line) {
        line = line.trim();

        String[] args = line.split(":");

        if (args.length == 1) {
            TestRequestItem info = new TestRequestItem(defalutMid, args[0]);
            requestInfo.add(info);
        } else if (args.length > 2) {
            int mid = Integer.parseInt(args[0]);
            TestRequestItem info = new TestRequestItem(mid, args[1]);
            requestInfo.add(info);
        }
    }

    private void loadMd5LiteString(String str) {
        String array[] = str.split(",");

        for (String i : array) {
            addRequest(i);
        }
    }

    private void loadMd5LiteFile(String fileName) {
        FileInputStream in;
        try {
            in = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {

            logger.error("read failed. name:" + fileName, e);
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                addRequest(line);
            }
        } catch (IOException e) {
            logger.error("read failed.", e);
        }
    }

    void initialRequest() {
        long start;
        long end;
        long used;

        long sleep;

        while (running) {
            start = System.currentTimeMillis();

            for (int i = 0; i < qps; i++) {
                ArrayList<TestRequestItem> req = getRequest();

                executor.execute(new TestTask(req));
                initialCounter.increment();
            }

            end = System.currentTimeMillis();

            used = end - start;

            sleep = 1000 - used;

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {

                }
            } else if (sleep < 0) {
                logger.error("cant initial request " + qps + " per second.");
            }
        }
    }

    void doRequest(TestRequest req) {
        long start = 0;
        long end = 0;
        long diff = 0;

        try {
            launchCounter.increment();

            start = System.currentTimeMillis();

            D6ResponseMessage result = super.request(req);

            end = System.currentTimeMillis();

            diff = end - start;

            if (result.getDocs().isEmpty()) {
                emptyCounter.increment(diff);
            }

            successCounter.increment(diff);

        } catch (Exception e) {
            end = System.currentTimeMillis();
            diff = end - start;

            failedCounter.increment();
        }
    }

    void dumpInfo() {
        while (running) {
            List<String> dumpContent = CounterManager.getInstance().dump();

            for (String l : dumpContent) {

                // System.out.println(l);
                logger.info(l);
            }

            Date now = new Date();

            if (now.after(stopTime)) {
                running = false;

                executor.shutdown();

                break;
            }

            try {
                Thread.sleep(dumpInterval);
            } catch (InterruptedException e) {

            }
        }

        logger.info("press test stop.");
    }

    protected D6ResponseMessageFactory createFactory() {
        return null;
    }

    @SuppressWarnings("static-access")
    protected void init() throws ExitExeption {
        super.init();

        options.addOption(OptionBuilder.withLongOpt("file").withDescription("md5 file name.").hasArg()
                .withArgName("file").create('f'));

        options.addOption(OptionBuilder.withLongOpt("md5Lite").withDescription("md5Lite list. split by ','").hasArg()
                .withArgName("md5Lite").create('m'));

        options.addOption(OptionBuilder.withLongOpt("thread").withDescription("max thread. default:10").hasArg()
                .withArgName("thread").create('t'));

        options.addOption(OptionBuilder.withLongOpt("qps").withDescription("request per secode. default:10").hasArg()
                .withArgName("qps").create('q'));

        options.addOption(OptionBuilder.withLongOpt("docPerReq")
                .withDescription("document size per request. default:5").hasArg().withArgName("docPerReq").create());

        options.addOption(OptionBuilder.withLongOpt("time").withDescription("test time(unit [hms]). default:1m")
                .hasArg().withArgName("time").create('l'));

        options.addOption(OptionBuilder.withLongOpt("dumpInterval")
                .withDescription("dump interval(unit [hms]). default:1m").hasArg().withArgName("dumpInterval")
                .create('p'));

        options.addOption(OptionBuilder.withLongOpt("sequence").withDescription("generate request mode")
                .withArgName("sequence").create());

        options.addOption(OptionBuilder.withLongOpt("random").withDescription("generate request mode")
                .withArgName("random").create());
    }

    protected void parseArgs(CommandLine cmd) throws ExitExeption {
        super.parseArgs(cmd);

        if (cmd.hasOption("thread")) {
            threadNumber = Integer.parseInt(cmd.getOptionValue("thread"));
        }

        if (cmd.hasOption("qps")) {
            qps = Integer.parseInt(cmd.getOptionValue("qps"));
        }

        if (cmd.hasOption("docPerReq")) {
            docPerReq = Integer.parseInt(cmd.getOptionValue("docPerReq"));
        }

        if (cmd.hasOption("time")) {
            String value = cmd.getOptionValue("time");
            timeTime = Integer.parseInt(value);

            if (value.endsWith("h")) {
                timeTime *= 60;
                timeTime *= 60;
                timeTime *= 1000;

            } else if (value.endsWith("m")) {
                timeTime *= 60;
                timeTime *= 1000;
            } else {
                timeTime *= 1000;
            }
        }

        if (cmd.hasOption("dumpInterval")) {
            String value = cmd.getOptionValue("dumpInterval");
            dumpInterval = Integer.parseInt(value);

            if (value.endsWith("h")) {
                dumpInterval *= 60;
                dumpInterval *= 60;
                dumpInterval *= 1000;

            } else if (value.endsWith("m")) {
                dumpInterval *= 60;
                dumpInterval *= 1000;
            } else {
                dumpInterval *= 1000;
            }
        }

        if (cmd.hasOption("sequence")) {
            mode = 1;
        }

        if (cmd.hasOption("random")) {
            mode = 0;
        }

        if (cmd.hasOption("file")) {
            loadMd5LiteFile(cmd.getOptionValue("file"));
            return;
        }

        if (cmd.hasOption("md5Lite")) {
            loadMd5LiteString(cmd.getOptionValue("md5Lite"));
            return;
        }
    }

    @Override
    protected void run(CommandLine cmd) throws ExitExeption {
        //
        logger.info("press test start.");
        logger.info("size of request doc:" + requestInfo.size());

        //
        service.open(super.createFactory());

        // set stop time
        long stopTimeMillis = System.currentTimeMillis() + timeTime;

        stopTime = new Date(stopTimeMillis);

        // start times
        CounterManager.getInstance().start();

        //
        executor = Executors.newFixedThreadPool(threadNumber);

        // start intial thread
        initialThread.start();

        // start dump thread
        dumpTread.start();
    }

    protected void stop() {
        super.stop();

        logger.info("press test stop.");

        executor.shutdown();
    }

    public static void main(String[] args) {
        ClientBase client = new Benchmark();

        client.entry(args);
    }
}
