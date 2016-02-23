package com.hcse.app.client.d6;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;

import com.hcse.app.client.ExitExeption;
import com.hcse.app.client.d6.ext.D6ResponseMessageFactory4ExtClient;
import com.hcse.app.client.d6.ext.D6ResponseMessageFactory4ExtJsonClient;
import com.hcse.app.client.d6.ext.D6ResponseMessageFactory4ExtJsonLogistic;
import com.hcse.d6.protocol.codec.D6ClientCodecFactory;

public class ExtClient extends Client {
    private String[] extFieldName;
    private static ExtClient _instance;

    private ExtClient() {
        _instance = this;
    }

    public String[] getExtFieldName() {
        return extFieldName;
    }

    public void setExtFieldName(String[] extFieldName) {
        this.extFieldName = extFieldName;
    }

    public static ExtClient getInstance() {
        return _instance;
    }

    protected D6ClientCodecFactory createCodecFactory() {
        if (version != 3) {
            if (app.equals("base")) {
                return new D6ClientCodecFactory(new D6ResponseMessageFactory4ExtClient());
            } else {
                return new D6ClientCodecFactory(new D6ResponseMessageFactory4ExtJsonLogistic());
            }
        } else {
            return new D6ClientCodecFactory(new D6ResponseMessageFactory4ExtJsonClient());
        }
    }

    @SuppressWarnings("static-access")
    protected void init() throws ExitExeption {
        super.init();

        options.addOption(OptionBuilder.withLongOpt("--extfields").withDescription("extend fields name:XA,XB,XC,XD")
                .hasArg().withArgName("extfields").create('e'));
    }

    protected void parseArgs(CommandLine cmd) throws ExitExeption {
        super.parseArgs(cmd);

        String extfields = "XA,XB,XC,XD";

        if (cmd.hasOption("extfields")) {
            extfields = cmd.getOptionValue("extfields");
        }

        extFieldName = extfields.split(",");
    }

    public static void main(String[] args) {
        ExtClient client = new ExtClient();

        client.entry(args);
    }
}
