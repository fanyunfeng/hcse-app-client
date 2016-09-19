package com.hcse.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface Extension {
    void init(Options options, BaseClient ctx);

    void parseArgs(CommandLine cmd, BaseClient ctx);
}
