package com.hcse.app.client.d6;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import com.hcse.app.client.CommonClient;
import com.hcse.app.client.ExitExeption;
import com.hcse.d6.protocol.codec.D6ClientCodecFactory;
import com.hcse.d6.protocol.factory.D6ResponseMessageFactory4Client;
import com.hcse.d6.protocol.factory.D6ResponseMessageFactory4JsonClient;
import com.hcse.d6.protocol.factory.D6ResponseMessageFactory4Logistic;
import com.hcse.d6.protocol.message.D6RequestMessage;
import com.hcse.d6.protocol.message.D6RequestMessageDoc;
import com.hcse.d6.protocol.message.D6RequestMessageV1;
import com.hcse.d6.protocol.message.D6RequestMessageV2;
import com.hcse.d6.protocol.message.D6RequestMessageV3;
import com.hcse.d6.protocol.message.D6ResponseMessage;
import com.hcse.d6.service.DataServiceImpl;
import com.hcse.protocol.util.packet.BasePacket;
import com.hcse.service.ServiceException;
import com.hcse.service.common.ServiceDiscoveryService;

public abstract class ClientBase extends CommonClient {
    public final Logger logger = Logger.getLogger(ClientBase.class);

    protected int version = 1;
    protected DataServiceImpl service = new DataServiceImpl();
    protected int defalutMid = 1;
    protected final String searchStr = "[S](([TX:TP:测试]))&([CL:CC:080])";
    public ClientBase() {
        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        service.setServiceDiscoveryService(serviceDiscovery);
    }

    protected String getDefaultUrl(){
    	return "data://127.0.0.1:3000";
    }
    
    @SuppressWarnings("static-access")
	protected void init() throws ExitExeption {
        options.addOption(OptionBuilder.withLongOpt("mid").withDescription("default machine id. default=1").hasArg()
                .withArgName("mid").create());

        options.addOption(OptionBuilder.withLongOpt("version").withDescription("version of request <[1],2,3>.")
                .hasArg().withArgName("version").create());
    }

    protected void parseArgs(CommandLine cmd) throws ExitExeption {
        if (cmd.hasOption("mid")) {
            try {
                defalutMid = Integer.parseInt(cmd.getOptionValue("mid"));
            } catch (NumberFormatException e) {
                logger.error("parse parameter mid failed. [" + cmd.getOptionValue("mid") + "]");
            }
        }

        if (cmd.hasOption("version")) {
            String value = cmd.getOptionValue("version");

            try {
                version = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error("parse parameter version failed. [" + cmd.getOptionValue("version") + "]");
            }
        }
    }

    protected D6ClientCodecFactory createCodecFactory() {
        if (version != 3) {
            if (app.equals("base")) {
                return new D6ClientCodecFactory(new D6ResponseMessageFactory4Client());
            } else {
                return new D6ClientCodecFactory(new D6ResponseMessageFactory4Logistic());
            }
        } else {
            return new D6ClientCodecFactory(new D6ResponseMessageFactory4JsonClient());
        }
    }

    protected D6ResponseMessage requestV1(TestRequest req) throws MalformedURLException, ServiceException {
        D6RequestMessage request = new D6RequestMessageV1(searchStr);

        request.setDocsCount(req.size());

        for (TestRequestItem i : req.getRequestItemList()) {
            D6RequestMessageDoc doc = request.getDocById(0);
            doc.setMachineId(i.mid);
            doc.setMd5Lite(i.md5Lite);
            doc.setIndentCount(0);
            doc.setWeight(0);
        }

        request.setServiceAddress(url);

        return service.search(request, createCodecFactory());
    }

    protected D6ResponseMessage requestV2(TestRequest req) throws MalformedURLException, ServiceException {
        D6RequestMessage request = new D6RequestMessageV2(searchStr);

        request.setDocsCount(req.size());

        for (TestRequestItem i : req.getRequestItemList()) {
            D6RequestMessageDoc doc = request.getDocById(0);
            doc.setMachineId(i.mid);
            doc.setMd5Lite(i.md5Lite);
            doc.setIndentCount(0);
            doc.setWeight(0);
        }

        request.setServiceAddress(url);

        return service.search(request, createCodecFactory());
    }

    protected D6ResponseMessage requestV3(TestRequest req) throws MalformedURLException, ServiceException {
        D6RequestMessage request = new D6RequestMessageV3(searchStr);

        request.setDocsCount(req.size());

        for (TestRequestItem i : req.getRequestItemList()) {
            D6RequestMessageDoc doc = request.getDocById(0);
            doc.setMachineId(i.mid);
            doc.setMd5Lite(i.md5Lite);
            doc.setIndentCount(0);
            doc.setWeight(0);
        }

        request.setServiceAddress(url);

        D6ResponseMessage response = service.search(request, createCodecFactory());

        if (response == null) {
            return null;
        }

        List<TestRequestItem> listRequestItems = req.getRequestItemList();
        List<BasePacket> pktList = response.getDocs();

        if (pktList.size() > 0) {
            int i = 0;

            for (BasePacket pkt : pktList) {
                pkt.getDocument().setMd5LiteString(listRequestItems.get(i).md5Lite);
            }
        }

        return response;
    }

    protected D6ResponseMessage request(TestRequest req) throws MalformedURLException, ServiceException {
        switch (version) {
        case 1:
            return requestV1(req);
        case 2:
            return requestV2(req);
        case 3:
            return requestV3(req);
        }

        return null;
    }

    protected D6ResponseMessage request(int mid, String md5Lite) throws MalformedURLException, ServiceException {
        TestRequest req = TestRequest.createSimple(mid, md5Lite);
        return request(req);
    }
}
