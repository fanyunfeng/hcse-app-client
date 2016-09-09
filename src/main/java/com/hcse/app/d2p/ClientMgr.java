package com.hcse.app.d2p;

import com.hcse.app.BaseClientConf;
import com.hcse.app.BaseClient;
import com.hcse.app.BenchmarkClient;
import com.hcse.app.BenchmarkConf;
import com.hcse.app.CommonClient;
import com.hcse.app.ExitException;
import com.hcse.app.RandomRequestQueue;
import com.hcse.app.RequestQueue;
import com.hcse.app.SequenceRequestQueue;
import com.hcse.protocol.d2.codec.D2ClientCodecFactory;
import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.protocol.d2.message.D2ResponseMessage;

public class ClientMgr extends com.hcse.app.d2.ClientMgr {

    @Override
    public BaseClientConf createConf() {
        return new BenchmarkConf();
    }

    @Override
    public BaseClient createContext() {
        return new D2BenchmarkClient();
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
    public void run(BaseClientConf conf, BaseClient ctx) throws ExitException {
        ctx.run(conf);
    }
}
