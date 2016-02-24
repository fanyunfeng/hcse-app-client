package com.hcse.app.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;

public class AppContext implements IApp {
    public HashMap<String, IApp> appMap = new HashMap<String, IApp>();
    public List<IApp> appList = new ArrayList<IApp>();

    void regiest(IApp app) {
        appMap.put(app.getClass().getName(), app);
        appList.add(app);
    }

    @Override
    public void init() throws ExitExeption {
        for (IApp i : appList) {
            i.init();
        }
    }

    @Override
    public void parseArgs(CommandLine cmd) throws ExitExeption {
        for (IApp i : appList) {
            i.parseArgs(cmd);
        }
    }

    @Override
    public void run(CommandLine cmd) throws ExitExeption {
        for (IApp i : appList) {
            i.run(cmd);
        }
    }
}
