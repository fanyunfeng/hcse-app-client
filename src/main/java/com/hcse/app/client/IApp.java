package com.hcse.app.client;

import org.apache.commons.cli.CommandLine;

public interface IApp {
    void init() throws ExitExeption;

    void parseArgs(CommandLine cmd) throws ExitExeption;

    void run(CommandLine cmd) throws ExitExeption;
}
