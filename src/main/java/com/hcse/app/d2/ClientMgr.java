package com.hcse.app.d2;

import java.io.OutputStream;

import com.hcse.app.BaseClientConf;
import com.hcse.app.BaseClient;
import com.hcse.app.CommonClient;
import com.hcse.app.CommonClientRunner;
import com.hcse.app.ExitException;
import com.hcse.app.FileRequestLoader;
import com.hcse.app.RequestQueue;
import com.hcse.app.SequenceRequestQueue;
import com.hcse.app.SingleRequestLoader;
import com.hcse.protocol.d2.codec.D2ClientCodecFactory;
import com.hcse.protocol.d2.factory.D2ResponseMessageFactory;
import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.protocol.dump.ConsoleOutputStreamBuilder;
import com.hcse.protocol.dump.D2ResponseDump;
import com.hcse.protocol.dump.FileOutputStreamBuilder;
import com.hcse.protocol.dump.MLDFileOutputStreamBuilder;
import com.hcse.protocol.dump.OutputStreamBuilder;
import com.hcse.protocol.handler.ConstantWeight;
import com.hcse.service.common.ServiceDiscoveryService;
import com.hcse.service.d2.IndexServiceImpl;
import com.hcse.util.sstring.RequestFactory;

public class ClientMgr extends com.hcse.app.ClientMgr {

    @Override
    public BaseClientConf createConf() {
        return super.createConf();
    }

    @Override
    public BaseClient createContext() {
        return new D2Client();
    }

    @Override
    public void init(BaseClientConf conf, BaseClient client) throws ExitException {
        super.init(conf, client);

        if (conf.file != null) {

        }
    }

    @Override
    public void parseArg(BaseClientConf conf, BaseClient client) throws ExitException {
        super.parseArg(conf, client);
    }

    public void createService(BaseClientConf conf, CommonClient client) {
        IndexServiceImpl srv = new IndexServiceImpl();

        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        srv.setServiceDiscoveryService(serviceDiscovery);

        client.setService(srv);
    }

    protected void createRequestLoader(BaseClientConf conf, CommonClient client) throws ExitException {
        if (conf.file != null) {
            client.setRequestLoader(new FileRequestLoader(conf.file));
        } else if (conf.searchString != null) {
            client.setRequestLoader(new SingleRequestLoader(conf.searchString));
        } else {
            throw new ExitException();
        }
    }

    protected void createCodecFactory(BaseClientConf conf, CommonClient client) throws ExitException {
        D2ClientCodecFactory factory = new D2ClientCodecFactory(new D2ResponseMessageFactory());

        client.setClientCodecFactory(factory);
    }

    protected void createRequestFactory(BaseClientConf conf, CommonClient client) throws ExitException {
        final D2RequestMessage request = new D2RequestMessage();

        if (conf.url != null) {
            request.setServiceAddress(conf.url);
        }

        RequestFactory<D2RequestMessage> requestFactory = new RequestFactory<D2RequestMessage>() {

            @Override
            public D2RequestMessage create() {
                try {
                    return (D2RequestMessage) request.clone();
                } catch (CloneNotSupportedException e) {
                    throw new Error(e);
                }
            }

        };
        client.setRequestFactory(requestFactory);
    }

    protected void createOutputStreamBuilder(BaseClientConf conf, CommonClient client) throws ExitException {
        OutputStreamBuilder outputStreamBuilder;
        if (conf.mld != null) {
            String dir = conf.dir;
            if (dir == null) {
                dir = "./result";
            }
            outputStreamBuilder = new MLDFileOutputStreamBuilder(dir, conf.mld, "json");
        } else if (conf.dir != null) {
            outputStreamBuilder = new FileOutputStreamBuilder(conf.dir, "json");
        } else {
            if (conf.silent) {
                outputStreamBuilder = new OutputStreamBuilder() {

                    @Override
                    public OutputStream creatOutputStream(long tag) {
                        return null;
                    }

                    @Override
                    public void destory(OutputStream os) {

                    }
                };
            } else {
                outputStreamBuilder = new ConsoleOutputStreamBuilder();
            }
        }

        client.setOutputStreamBuilder(outputStreamBuilder);
    }

    protected void createResponseDump(BaseClientConf conf, CommonClient client) throws ExitException {
        D2ResponseDump dumper = new D2ResponseDump();

        dumper.setCharset(conf.charset);
        dumper.setPretty(conf.pretty);

        client.setDumper(dumper);
    }

    protected void createRequestQueue(BaseClientConf conf, final CommonClient client) throws ExitException {
        RequestQueue<D2RequestMessage> queue = new SequenceRequestQueue<D2RequestMessage>();

        client.setRequestQueue(queue);
    }

    protected void addDocHandler(BaseClientConf conf, BaseClient client) throws ExitException {
        ConstantWeight handler = new ConstantWeight();

        client.addDocHandler(handler);
    }

    protected void createClientRunner(BaseClientConf conf, CommonClient client) throws ExitException {

        client.setClientRunner(new CommonClientRunner());
    }

    @Override
    public void prepare(BaseClientConf conf, BaseClient _client) throws ExitException {
        super.prepare(conf, _client);

        CommonClient client = (CommonClient) _client;

        createService(conf, client);

        createRequestFactory(conf, client);

        createClientRunner(conf, client);

        createRequestLoader(conf, client);

        createCodecFactory(conf, client);

        createOutputStreamBuilder(conf, client);

        createResponseDump(conf, client);

        createRequestQueue(conf, client);

        addDocHandler(conf, client);

        client.init();
    }

    public void run(BaseClientConf conf, BaseClient ctx) throws ExitException {
        ctx.run(conf);

        ctx.stop();
    }
}
