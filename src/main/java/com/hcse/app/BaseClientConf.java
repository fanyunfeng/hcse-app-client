package com.hcse.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

enum DocContentFormat {
    array, object
};

public class BaseClientConf {
    protected final static Logger logger = Logger.getLogger(BaseClientConf.class);

    protected Options options = new Options();
    protected CommandLine commandLine;

    protected ExtClientConf extConfig;

    void setExtClientConf(ExtClientConf ext) {
        extConfig = ext;
    }

    public Options getOptions() {
        return options;
    }

    public void parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        commandLine = parser.parse(options, args);
    }

    public void init() {
        options.addOption(new Option("h", "help", false, "print this message"));

        // arguments
        options.addOption(OptionBuilder.withLongOpt("url").withDescription("url of service: data://127.0.0.1:3000")
                .hasArg().withArgName("url").create('u'));

        options.addOption(OptionBuilder.withLongOpt("file").withDescription("search string file name.").hasArg()
                .withArgName("file").create('f'));

        options.addOption(OptionBuilder.withLongOpt("searchString").withDescription("search string.").hasArg()
                .withArgName("searchString").create('s'));

        options.addOption(OptionBuilder.withLongOpt("directory").withDescription("directory to save result").hasArg()
                .withArgName("directory").create('d'));

        options.addOption(OptionBuilder.withLongOpt("charset").withDescription("charset to encoding JSON.").hasArg()
                .withArgName("charset").create());

        options.addOption(OptionBuilder.withLongOpt("mld").withDescription("save result by MLD mode.").hasArg()
                .withArgName("mld").create());

        // options
        options.addOption(OptionBuilder.withLongOpt("pretty").withDescription("print pretty format. true/false")
                .hasArg().withArgName("pretty").create());

        options.addOption(OptionBuilder.withLongOpt("array").withDescription("print document field by json array.")
                .withArgName("array").create());

        options.addOption(OptionBuilder.withLongOpt("object").withDescription("print document field by json array.")
                .withArgName("object").create());

        if (extConfig != null) {
            extConfig.init();
        }
    }

    public void printHelp(String name) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);

        hf.printHelp(name, options, false);
    }

    public void parse() throws ExitException {
        if (commandLine.hasOption("help")) {
            throw new ShowHelpException();
        }

        if (commandLine.hasOption("file")) {
            file = commandLine.getOptionValue("file");
        }

        if (commandLine.hasOption("searchString")) {
            searchString = commandLine.getOptionValue("searchString");
        }

        if (commandLine.hasOption("directory")) {
            save = true;
            dir = commandLine.getOptionValue("directory");
        }

        if (commandLine.hasOption("pretty")) {
            String value = commandLine.getOptionValue("pretty");

            value = value.toLowerCase();

            if (value.equals("yes") || value.equalsIgnoreCase("true") || value.equals("1")) {
                pretty = true;
            }
        }

        if (commandLine.hasOption("charset")) {
            charset = commandLine.getOptionValue("charset");
        }

        if (commandLine.hasOption("array")) {
            fieldFormat = DocContentFormat.array;
        }

        if (commandLine.hasOption("object")) {
            fieldFormat = DocContentFormat.object;
        }

        if (commandLine.hasOption("url")) {
            url = commandLine.getOptionValue("url");
        }

        if (commandLine.hasOption("version")) {
            String value = commandLine.getOptionValue("version");

            try {
                version = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error("parse parameter version failed. [" + commandLine.getOptionValue("version") + "]");
            }
        }

        if (commandLine.hasOption("app")) {
            String value = commandLine.getOptionValue("app");

            value = value.toLowerCase();

            if (value.equals("base") || value.equals("logistic")) {
                app = value;
            }
        }

        if (commandLine.hasOption("mld")) {
            String value = commandLine.getOptionValue("mld");

            String[] values = value.split(":");

            try {
                if (value.length() > 0) {
                    int ivalues[] = null;

                    if (value.endsWith(":")) {
                        ivalues = new int[values.length + 1];
                        ivalues[values.length] = -1;
                    } else {
                        ivalues = new int[values.length];
                    }

                    for (int i = 0; i < values.length; i++) {
                        ivalues[i] = Integer.parseInt(values[i]);
                    }

                    mld = ivalues;

                }
            } catch (NumberFormatException e) {

            }
        }

        if (extConfig != null) {
            extConfig.parse();
        }
    }

    public String url;
    public String app;
    public int version = 0;

    public String file;
    public String dir;
    public String searchString;

    public boolean save = false;
    public boolean pretty = true;
    public int firstLevelLength = 3;

    public String charset = "utf8";
    public DocContentFormat fieldFormat;

    public int[] mld;

    public boolean silent = false;
}
