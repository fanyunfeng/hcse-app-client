package com.hcse.app.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

public abstract class CommonClient {
	protected final Logger logger = Logger.getLogger(CommonClient.class);
	
	
	protected Options options = new Options();
	protected String app = "base";
	protected volatile boolean running = true;

	protected String url;
	protected void stop() {
	    running = false;
	}
	
	protected abstract void run(CommandLine cmd) throws ExitExeption;

	protected abstract String getDefaultUrl();
	
    @SuppressWarnings("static-access")
	protected void init() throws ExitExeption {
        options.addOption(new Option("h", "help", false, "print this message"));

        options.addOption(OptionBuilder.withLongOpt("url").withDescription("url of service: " + getDefaultUrl())
                .hasArg().withArgName("url").create('u'));

        options.addOption(OptionBuilder.withLongOpt("app").withDescription("app type <[base], logistic>.").hasArg()
                .withArgName("app").create());

        options.addOption(OptionBuilder.withLongOpt("verbose").withDescription("output debug infomoration.")
                .withArgName("verbose").create('v'));
    }

    protected void parseArgs(CommandLine cmd) throws ExitExeption {
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);

            hf.printHelp("d6.client", options, false);
            throw new ExitExeption();
        }

        if (cmd.hasOption("url")) {
            url = cmd.getOptionValue("url");
        }

        if (cmd.hasOption("app")) {
            String value = cmd.getOptionValue("app");

            value = value.toLowerCase();

            if (value.equals("base") || value.equals("logistic")) {
                app = value;
            }
        }
    }

	protected void entry(String[] args) {
	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        @Override
	        public void run() {
	            stop();
	        }
	    }));
	
	    CommandLineParser parser = new PosixParser();
	
	    try {
	        init();
	
	        CommandLine cmd = parser.parse(options, args);
	
	        parseArgs(cmd);
	
	        run(cmd);
	
	    } catch (ParseException e) {
	        logger.error("ParseException", e);
	    } catch (ExitExeption e) {
	
	    }
	}
}
