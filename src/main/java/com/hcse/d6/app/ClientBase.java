package com.hcse.d6.app;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.hcse.d6.protocol.factory.D6ResponseMessageFactory;
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
import com.hcse.service.common.ServiceDiscoveryService;

public abstract class ClientBase {
    protected Options options = new Options();
    protected String app = "base";
    protected String url = "data://127.0.0.1:3000";
    protected int version = 1;
    protected DataServiceImpl service = new DataServiceImpl();
    protected int defalutMid = 1;
    protected final String searchStr = "[S](([TX:TP:测试]))&([CL:CC:080])";

    public ClientBase() {
        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        service.setServiceDiscoveryService(serviceDiscovery);
    }

    @SuppressWarnings("static-access")
    protected void init() throws ExitExeption {
        options.addOption(new Option("h", "help", false, "print this message"));

        options.addOption(OptionBuilder.withLongOpt("url").withDescription("url of service: data://127.0.0.1:3000")
                .hasArg().withArgName("url").create('u'));

        options.addOption(OptionBuilder.withLongOpt("app").withDescription("app type <[base], logistic>.").hasArg()
                .withArgName("app").create());

        options.addOption(OptionBuilder.withLongOpt("mid").withDescription("default machine id. default=1").hasArg()
                .withArgName("mid").create());

        options.addOption(OptionBuilder.withLongOpt("version").withDescription("version of request <[1],2,3>.")
                .hasArg().withArgName("version").create());

        options.addOption(OptionBuilder.withLongOpt("verbose").withDescription("output debug infomoration.")
                .withArgName("verbose").create('v'));
    }

    protected void parseArgs(CommandLine cmd) throws ExitExeption {
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);

            hf.printHelp("d6.client", options, false);
            throw new ExitExeption();
        }

        if (cmd.hasOption("url")) {
            url = cmd.getOptionValue("url");
        }

        if (cmd.hasOption("version")) {
            String value = cmd.getOptionValue("version");

            try {
                version = Integer.parseInt(value);
            } catch (NumberFormatException e) {

            }
        }

        if (cmd.hasOption("app")) {
            String value = cmd.getOptionValue("app");

            value = value.toLowerCase();

            if (value.equals("base") || value.equals("logistic")) {
                app = value;
            }
        }
    }

    protected abstract void run(CommandLine cmd) throws ExitExeption;

    protected void entry(String[] args) {
        CommandLineParser parser = new PosixParser();

        try {
            init();

            CommandLine cmd = parser.parse(options, args);

            parseArgs(cmd);

            run(cmd);

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ExitExeption e) {

        }
    }

    protected D6ResponseMessageFactory createFactory() {
        if (version != 3) {
            if (app.equals("base")) {
                return new D6ResponseMessageFactory4Client();
            } else {
                return new D6ResponseMessageFactory4Logistic();
            }
        } else {
            return new D6ResponseMessageFactory4JsonClient();
        }
    }

    protected D6ResponseMessage requestV1(TestRequest req) throws MalformedURLException {
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

        return service.search(request, createFactory());
    }

    protected D6ResponseMessage requestV2(TestRequest req) throws MalformedURLException {
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

        return service.search(request, createFactory());
    }

    protected D6ResponseMessage requestV3(TestRequest req) throws MalformedURLException {
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

        D6ResponseMessage response = service.search(request, createFactory());

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

    protected D6ResponseMessage request(TestRequest req) throws MalformedURLException {
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

    protected D6ResponseMessage request(int mid, String md5Lite) throws MalformedURLException {
        TestRequest req = TestRequest.createSimple(mid, md5Lite);
        return request(req);
    }
}
