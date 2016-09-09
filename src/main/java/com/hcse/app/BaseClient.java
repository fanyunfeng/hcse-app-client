package com.hcse.app;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class BaseClient {
    protected final static Logger logger = Logger.getLogger(BaseClient.class);

    private BaseClientConf config;
    private BaseClientContext context;

    private String name;

    public BaseClientConf createConf() {
        return new BaseClientConf();
    }

    public BaseClientContext createContext() {
        return new BaseClientContext();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void init(BaseClientConf conf, BaseClientContext ctx) throws ExitException {
        config.init();
    }

    public void parseArg(BaseClientConf conf, BaseClientContext ctx) throws ExitException {
        conf.parse();
    }

    public void prepare(BaseClientConf conf, BaseClientContext ctx) throws ExitException {

    }

    public void run(BaseClientConf conf, BaseClientContext ctx) throws ExitException {

    }

    public void stop(BaseClientConf conf, BaseClientContext ctx) {

    }

    private void entry(String[] args) {
        config = createConf();
        context = createContext();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                stop(config, context);
            }
        }));

        try {
            init(config, context);

            config.parseArgs(args);

            parseArg(config, context);

            prepare(config, context);

            run(config, context);

        } catch (ParseException e) {
            config.printHelp(name);
        } catch (ShowHelpException e) {
            config.printHelp(name);
        } catch (ExitException e) {
            logger.error("ExitExeption", e);

            System.exit(1);
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.exit(1);
        }

        try {
            String appClass = "com.hcse.app." + args[0] + ".Client";

            BaseClient client;

            client = (BaseClient) BaseClient.class.getClassLoader().loadClass(appClass).newInstance();

            client.setName(args[0]);

            String[] nargs = new String[args.length - 1];

            for (int i = 1; i < args.length; i++) {
                nargs[i - 1] = args[i];
            }

            client.entry(nargs);

        } catch (InstantiationException e) {
            logger.error("ExitExeption", e);
        } catch (IllegalAccessException e) {
            logger.error("ExitExeption", e);
        } catch (ClassNotFoundException e) {
            logger.error("ExitExeption", e);
        }
    }
}
