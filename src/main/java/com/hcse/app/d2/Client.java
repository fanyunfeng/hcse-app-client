package com.hcse.app.d2;

import com.hcse.app.BaseClient;
import com.hcse.app.BaseClientConf;
import com.hcse.app.BaseClientContext;
import com.hcse.app.d6.ExitExeption;
import com.hcse.protocol.dump.ConsoleOutputStreamBuilder;
import com.hcse.protocol.dump.D2ResponseDump;
import com.hcse.protocol.dump.FileOutputStreamBuilder;
import com.hcse.protocol.dump.MLDFileOutputStreamBuilder;
import com.hcse.protocol.dump.OutputStreamBuilder;
import com.hcse.protocol.handler.ConstantWeight;

public class Client extends BaseClient {

    @Override
    public BaseClientConf createConf() {
        return super.createConf();
    }

    @Override
    public BaseClientContext createContext() {
        return new D2Client();
    }

    @Override
    public void init(BaseClientConf conf, BaseClientContext ctx) throws ExitExeption {
        super.init(conf, ctx);

        if (conf.file != null) {

        }
    }

    @Override
    public void parseArg(BaseClientConf conf, BaseClientContext ctx) throws ExitExeption {
        super.parseArg(conf, ctx);
    }

    protected void createRequestLoader(BaseClientConf conf, D2Client client) throws ExitExeption {
        if (conf.file != null) {
            client.setRequestLoader(new FileResquestLoader(conf.file));
        } else if (conf.searchString != null) {
            client.setRequestLoader(new SingleRequestLoader(conf.searchString));
        } else {
            throw new ExitExeption();
        }
    }

    protected void createOutputStreamBuilder(BaseClientConf conf, D2Client client) throws ExitExeption {
        OutputStreamBuilder outputStreamBuilder;
        if (conf.mld != null) {
            String dir = conf.dir;
            if (dir == null) {
                dir = ".";
            }
            outputStreamBuilder = new MLDFileOutputStreamBuilder(conf.dir, conf.mld, "json");
        } else if (conf.dir == null) {
            outputStreamBuilder = new FileOutputStreamBuilder(conf.dir, "json");
        } else {
            outputStreamBuilder = new ConsoleOutputStreamBuilder();
        }

        client.setOutputStreamBuilder(outputStreamBuilder);
    }

    protected void createResponseDump(BaseClientConf conf, D2Client client) throws ExitExeption {
        D2ResponseDump dumper = new D2ResponseDump();

        dumper.setCharset(conf.charset);
        dumper.setPretty(conf.pretty);

        client.setDumper(dumper);
    }

    protected void createRequestQueue(BaseClientConf conf, D2Client client) throws ExitExeption {
        RequestQueue queue = new SequenceRequestQueue();

        client.setRequestQueue(queue);
    }

    protected void addDocHandler(BaseClientConf conf, D2Client client) throws ExitExeption {
        ConstantWeight handler = new ConstantWeight();

        client.addDocHandler(handler);
    }

    @Override
    public void prepare(BaseClientConf conf, BaseClientContext ctx) throws ExitExeption {
        super.prepare(conf, ctx);

        D2Client client = (D2Client) ctx;

        createRequestLoader(conf, client);

        createOutputStreamBuilder(conf, client);

        createResponseDump(conf, client);

        createRequestQueue(conf, client);

        addDocHandler(conf, client);
    }

    public void run(BaseClientConf conf, BaseClientContext ctx) throws ExitExeption {
        ctx.run(conf);
    }
}
