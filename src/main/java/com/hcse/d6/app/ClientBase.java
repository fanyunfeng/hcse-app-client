package com.hcse.d6.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public abstract class ClientBase {
    protected Options options = new Options();
    protected String app = "base";
    protected String url = "data://127.0.0.1:3000";
    protected int version = 1;

    @SuppressWarnings("static-access")
    protected void init() throws ExitExeption {
        options.addOption(new Option("h", "help", false, "print this message"));

        options.addOption(OptionBuilder.withLongOpt("url").withDescription("url of service: data://127.0.0.1:3000")
                .hasArg().withArgName("url").create('u'));

        options.addOption(OptionBuilder.withLongOpt("app").withDescription("app type <[base], logistic>.").hasArg()
                .withArgName("app").create());

        options.addOption(OptionBuilder.withLongOpt("version").withDescription("version of request <[1],2,3>.")
                .hasArg().withArgName("version").create());

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

        if (cmd.hasOption("version")) {
            String value = cmd.getOptionValue("version");

            try {
                version = Integer.parseInt(value);
            } catch (NumberFormatException e) {

            }
        }

        if (cmd.hasOption("app")) {
            String value = cmd.getOptionValue("app");

            value = value.toLowerCase();

            if (value.equals("base") || value.equals("logistic")) {
                app = value;
            }
        }
    }

    protected abstract void run(CommandLine cmd) throws ExitExeption;

    protected void entry(String[] args) {
        CommandLineParser parser = new PosixParser();

        try {
            init();

            CommandLine cmd = parser.parse(options, args);

            parseArgs(cmd);

            run(cmd);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ExitExeption e) {

        }
    }
}
