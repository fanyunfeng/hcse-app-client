package com.hcse.app.ext;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.hcse.app.BaseClient;
import com.hcse.app.Extension;
import com.hcse.protocol.handler.ConstantWeight;

public class ExtensionMgr {
    ArrayList<Extension> extensions = new ArrayList<Extension>();

    public ExtensionMgr() {
        extensions.add(new CommonExt(ConstantWeight.class, true));
    }

    public void init(Options options, BaseClient ctx) {
        for (Extension e : extensions) {
            e.init(options, ctx);
        }
    }

    public void parseArgs(CommandLine cmd, BaseClient ctx) {
        for (Extension e : extensions) {
            e.parseArgs(cmd, ctx);
        }
    }
}
