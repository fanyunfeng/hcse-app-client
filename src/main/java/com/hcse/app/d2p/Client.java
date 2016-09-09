package com.hcse.app.d2p;

import com.hcse.app.BaseClientConf;
import com.hcse.app.BaseClientContext;
import com.hcse.app.BenchmarkClient;
import com.hcse.app.BenchmarkConf;
import com.hcse.app.CommonClient;
import com.hcse.app.ExitException;
import com.hcse.app.d2.RandomRequestQueue;
import com.hcse.app.d2.RequestQueue;
import com.hcse.app.d2.SequenceRequestQueue;
import com.hcse.protocol.d2.message.D2RequestMessage;

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
    protected void createRequestQueue(BaseClientConf xconf, CommonClient client) throws ExitException {
        BenchmarkConf conf = (BenchmarkConf) xconf;
        RequestQueue<D2RequestMessage> queue;
        if (conf.mode == 0) {
            queue = new SequenceRequestQueue<D2RequestMessage>();
        } else {
            queue = new RandomRequestQueue<D2RequestMessage>();
        }

        client.setRequestQueue(queue);
    }

    @Override
    protected void createResponseDump(BaseClientConf conf, CommonClient client) throws ExitException {

    }

    @Override
    public void run(BaseClientConf conf, BaseClientContext ctx) throws ExitException {
        ctx.run(conf);
    }
}
