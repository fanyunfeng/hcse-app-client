package com.hcse.app.d2p;

import com.hcse.app.BaseClientConf;
import com.hcse.app.BaseClientContext;
import com.hcse.app.ExitException;

public class Client extends com.hcse.app.d2.Client {

    @Override
    public BaseClientConf createConf() {
        return new BenchmarkConf();
    }

    @Override
    public BaseClientContext createContext() {
        return new BenchmarkClient();
    }

    @Override
    public void run(BaseClientConf conf, BaseClientContext ctx) throws ExitException {
        ctx.run(conf);
    }
}
