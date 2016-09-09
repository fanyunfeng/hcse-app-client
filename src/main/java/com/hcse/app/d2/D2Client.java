package com.hcse.app.d2;

import com.hcse.app.BaseClientConf;
import com.hcse.app.CommonClient;
import com.hcse.app.ExitException;
import com.hcse.protocol.d2.codec.D2ClientCodecFactory;
import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.protocol.d2.message.D2ResponseMessage;
import com.hcse.protocol.dump.D2ResponseDump;
import com.hcse.service.common.ServiceDiscoveryService;
import com.hcse.service.d2.IndexServiceImpl;

public class D2Client extends CommonClient<D2RequestMessage, D2ResponseMessage, D2ClientCodecFactory> {

    protected void createResponseDump(BaseClientConf conf, CommonClient client) throws ExitException {
        D2ResponseDump dumper = new D2ResponseDump();

        dumper.setCharset(conf.charset);
        dumper.setPretty(conf.pretty);

        client.setDumper(dumper);
    }

    @Override
    public void createService() {
        IndexServiceImpl srv = new IndexServiceImpl();

        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        srv.setServiceDiscoveryService(serviceDiscovery);

        service = srv;
    }
}
