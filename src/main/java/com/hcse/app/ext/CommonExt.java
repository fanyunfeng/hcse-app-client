package com.hcse.app.ext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.hcse.app.BaseClient;
import com.hcse.app.Extension;
import com.hcse.protocol.handler.DocHandler;
import com.hcse.protocol.handler.ResponseHandler;

public class CommonExt implements Extension {
    private Class<?> zlass;
    private boolean def;

    private String withName;
    private String withoutName;

    public CommonExt(Class<?> name, boolean def) {
        this.zlass = name;
        this.def = def;

        withName = "with-" + zlass.getSimpleName();
        withoutName = "without-" + zlass.getSimpleName();
    }

    @Override
    public void init(Options options, BaseClient ctx) {

        if (!def) {
            options.addOption(null, withName, false, "enalbe " + zlass.getName());
        } else {
            options.addOption(null, withoutName, false, "disable " + zlass.getName());
        }
    }

    @Override
    public void parseArgs(CommandLine cmd, BaseClient ctx) {
        if (cmd.hasOption(withName) || (def && !cmd.hasOption(withoutName))) {
            try {
                Object handler = zlass.newInstance();

                if (handler instanceof DocHandler) {
                    DocHandler h = (DocHandler) handler;
                    ctx.addDocHandler(h);
                }

                if (handler instanceof ResponseHandler) {
                    ResponseHandler h = (ResponseHandler) handler;
                    ctx.addResponseHandler(h);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
