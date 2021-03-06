package com.hcse.app;

public class BenchmarkConf extends BaseClientConf {

    @Override
    public void init() {
        super.init();

        options.addOption("t", "thread", true, "max thread. default:100");
        options.addOption("q", "qps", true, "request per secode. default:100");
        options.addOption("l", "time", true, "test time(unit [hms]). default:1m");
        options.addOption(null, "sequence", false, "generate request mode.");
        options.addOption(null, "random", false, "generate request mode.");
    }

    @Override
    public void parse() throws ExitException {
        super.parse();

        if (commandLine.hasOption("thread")) {
            threadNumber = Integer.parseInt(commandLine.getOptionValue("thread"));
        }

        if (commandLine.hasOption("qps")) {
            qps = Integer.parseInt(commandLine.getOptionValue("qps"));
        }

        if (commandLine.hasOption("time")) {
            String value = commandLine.getOptionValue("time");
            testTime = Integer.parseInt(value);

            if (value.endsWith("h")) {
                testTime *= 60;
                testTime *= 60;
                testTime *= 1000;

            } else if (value.endsWith("m")) {
                testTime *= 60;
                testTime *= 1000;
            } else {
                testTime *= 1000;
            }
        }

        if (commandLine.hasOption("sequence")) {
            mode = 1;
        }

        if (commandLine.hasOption("random")) {
            mode = 0;
        }
    }

    public int mode = 0;
    public int threadNumber = 10;
    public int qps = 100;
    public long testTime = 1000 * 60;
}
