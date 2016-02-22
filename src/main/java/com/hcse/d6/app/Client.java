package com.hcse.d6.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

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
import com.hcse.protocol.util.packet.BaseDoc;
import com.hcse.protocol.util.packet.BasePacket;
import com.hcse.service.common.ServiceDiscoveryService;

enum DocContentFormat {
    array, object
};

public class Client {

    DataServiceImpl service = new DataServiceImpl();

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected DocContentFormat fieldFormat = DocContentFormat.array;
    protected String dir = ".";

    protected boolean save = false;
    protected int defalutMid = 1;
    protected String url = "data://127.0.0.1:3000";

    protected int version = 1;
    protected boolean pretty = true;
    protected int firstLevelLength = 3;

    protected String app = "base";

    protected final String searchStr = "[S](([TX:TP:测试]))&([CL:CC:080])";

    protected String charset = "utf8";

    public Client() {
        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        service.setServiceDiscoveryService(serviceDiscovery);
    }

    class MyPrettyPrinter extends DefaultPrettyPrinter {
        public MyPrettyPrinter() {
            this._arrayIndenter = new Lf2SpacesIndenter();
        }
    }

    public void dumpJsonDoc(OutputStream os, BaseDoc doc) {

        try {
            OutputStreamWriter writer;

            writer = new OutputStreamWriter(os, charset);

            JsonGenerator generator = objectMapper.getJsonFactory().createJsonGenerator(writer);

            if (pretty) {
                MyPrettyPrinter pp = new MyPrettyPrinter();
                generator.setPrettyPrinter(pp);
            }

            //
            generator.writeStartArray();
            {
                generator.writeStartObject();
                {
                    generator.writeStringField("md5Lite", Long.toHexString(doc.getMd5Lite()).toUpperCase());
                    generator.writeNumberField("weight", doc.getWeight());

                    switch (fieldFormat) {
                    case object: {
                        generator.writeObjectFieldStart("content");
                        {

                            List<String> values = doc.getValues();
                            List<String> names = doc.getNames();

                            Iterator<String> it = names.iterator();
                            for (String value : values) {
                                if (!it.hasNext()) {
                                    break;
                                }

                                String name = it.next();

                                if (!value.isEmpty()) {
                                    generator.writeStringField(name, value);
                                }
                            }

                        }
                        generator.writeEndObject();
                        break;
                    }
                    case array: {
                        generator.writeArrayFieldStart("content");
                        {
                            List<String> values = doc.getValues();
                            for (String value : values) {
                                generator.writeString(value);
                            }
                        }
                        generator.writeEndArray();
                        break;
                    }
                    }
                }
                generator.writeEndObject();
            }
            generator.writeEndArray();

            //
            generator.flush();
            writer.flush();

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private OutputStream createOutputStream(int mid, String md5) throws FileNotFoundException {
        if (!save) {
            return System.out;
        }

        String preDir = md5.substring(0, firstLevelLength);

        String pathName = String.format("%s%c%03d%c%s", dir, File.separatorChar, mid, File.separatorChar, preDir);

        File file = new File(pathName);

        if (!file.exists()) {
            file.mkdirs();
        }

        return new FileOutputStream(pathName + File.separatorChar + md5 + ".txt");
    }

    private void closeOutputStream(OutputStream os) {
        if (save) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getOneDoc(String midAndDoc) throws FileNotFoundException {
        String[] fields = midAndDoc.split(":");

        try {
            if (fields.length == 2) {
                getOneDoc(Integer.parseInt(fields[0]), fields[1]);
            } else {
                getOneDoc(defalutMid, fields[0]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
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

    private D6ResponseMessage requestV1(int mid, String md5) throws MalformedURLException {
        D6RequestMessage request = new D6RequestMessageV1(searchStr);

        request.setDocsCount(1);

        {
            D6RequestMessageDoc doc = request.getDocById(0);
            doc.setMachineId(mid);
            doc.setMd5Lite(md5);
            doc.setIndentCount(0);
            doc.setWeight(0);
        }

        request.setServiceAddress(url);

        return service.search(request, createFactory());
    }

    private D6ResponseMessage requestV2(int mid, String md5) throws MalformedURLException {
        D6RequestMessage request = new D6RequestMessageV2(searchStr);

        request.setDocsCount(1);

        {
            D6RequestMessageDoc doc = request.getDocById(0);
            doc.setMachineId(mid);
            doc.setMd5Lite(md5);
            doc.setIndentCount(0);
            doc.setWeight(0);
        }

        request.setServiceAddress(url);

        return service.search(request, createFactory());
    }

    private D6ResponseMessage requestV3(int mid, String md5Lite) throws MalformedURLException {
        D6RequestMessage request = new D6RequestMessageV3(searchStr);

        request.setDocsCount(1);

        {
            D6RequestMessageDoc doc = request.getDocById(0);
            doc.setMachineId(mid);
            doc.setMd5Lite(md5Lite);
            doc.setIndentCount(0);
            doc.setWeight(0);
        }

        request.setServiceAddress(url);

        D6ResponseMessage response = service.search(request, createFactory());

        if (response == null) {
            return null;
        }

        List<BasePacket> pktList = response.getDocs();

        if (pktList.size() > 0) {
            BasePacket pkt = pktList.get(0);

            pkt.getDocument().setMd5LiteString(md5Lite);
        }

        return response;
    }

    private D6ResponseMessage request(int mid, String md5Lite) throws MalformedURLException {
        switch (version) {
        case 1:
            return requestV1(mid, md5Lite);
        case 2:
            return requestV2(mid, md5Lite);
        case 3:
            return requestV3(mid, md5Lite);
        }

        return null;
    }

    private void getOneDoc(int mid, String md5Lite) throws FileNotFoundException {
        try {
            D6ResponseMessage response = request(mid, md5Lite);

            if (response == null) {
                return;
            }

            List<BasePacket> docs = response.getDocs();

            if (docs.isEmpty()) {
                return;
            }

            OutputStream os = createOutputStream(mid, md5Lite);

            dumpJsonDoc(os, docs.get(0).getDocument());

            closeOutputStream(os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDoc(String fileName) {
        FileInputStream in;
        try {
            in = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                getOneDoc(line);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private void getDoc(String[] md5LiteList) {
        for (String v : md5LiteList) {
            try {
                getOneDoc(v);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("static-access")
    public static void main(String[] args) {

        Options options = new Options();

        options.addOption(new Option("h", "help", false, "print this message"));

        options.addOption(OptionBuilder.withLongOpt("url").withDescription("url of service: data://127.0.0.1:3000")
                .hasArg().withArgName("url").create('u'));

        options.addOption(OptionBuilder.withLongOpt("file").withDescription("md5 file name.").hasArg()
                .withArgName("file").create('f'));

        options.addOption(OptionBuilder.withLongOpt("md5Lite").withDescription("md5Lite list split by ,").hasArg()
                .withArgName("md5Lite").create('m'));

        options.addOption(OptionBuilder.withLongOpt("directory").withDescription("directory to save result").hasArg()
                .withArgName("directory").create('d'));

        options.addOption(OptionBuilder.withLongOpt("version").withDescription("version of request <[1],2,3>.")
                .hasArg().withArgName("version").create());

        options.addOption(OptionBuilder.withLongOpt("app").withDescription("app type <[base], logistic>.").hasArg()
                .withArgName("app").create());

        options.addOption(OptionBuilder.withLongOpt("mid").withDescription("default machine id. default=0").hasArg()
                .withArgName("mid").create());

        options.addOption(OptionBuilder.withLongOpt("pretty").withDescription("print pretty format. true/false")
                .hasArg().withArgName("pretty").create());

        options.addOption(OptionBuilder.withLongOpt("array").withDescription("print document field by json array.")
                .withArgName("array").create());

        options.addOption(OptionBuilder.withLongOpt("object").withDescription("print document field by json array.")
                .withArgName("object").create());

        options.addOption(OptionBuilder.withLongOpt("charset").withDescription("charset to encoding JSON.")
                .withArgName("charset").create());

        options.addOption(OptionBuilder.withLongOpt("verbose").withDescription("output debug infomoration.")
                .withArgName("verbose").create('v'));

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.setWidth(110);

                hf.printHelp("d6.client", options, false);
                return;
            }

            Client client = new Client();

            if (cmd.hasOption("directory")) {
                client.save = true;
                client.dir = cmd.getOptionValue("directory");
            }

            if (cmd.hasOption("url")) {
                client.url = cmd.getOptionValue("url");
            }

            if (cmd.hasOption("version")) {
                String value = cmd.getOptionValue("version");

                try {
                    client.version = Integer.parseInt(value);
                } catch (NumberFormatException e) {

                }
            }

            if (cmd.hasOption("mid")) {
                try {
                    client.defalutMid = Integer.parseInt(cmd.getOptionValue("mid"));
                } catch (NumberFormatException e) {

                }
            }

            if (cmd.hasOption("app")) {
                String value = cmd.getOptionValue("app");

                value = value.toLowerCase();

                if (value.equals("base") || value.equals("logistic")) {
                    client.app = value;
                }
            }

            if (cmd.hasOption("pretty")) {
                String value = cmd.getOptionValue("pretty");

                value = value.toLowerCase();

                if (value.equals("yes") || value.equalsIgnoreCase("true") || value.equals("1")) {
                    client.pretty = true;
                }
            }

            if (cmd.hasOption("charset")) {
                client.charset = cmd.getOptionValue("charset");
            }

            if (cmd.hasOption("array")) {
                client.fieldFormat = DocContentFormat.array;
            }

            if (cmd.hasOption("object")) {
                client.fieldFormat = DocContentFormat.object;
            }

            if (cmd.hasOption("file")) {
                client.getDoc(cmd.getOptionValue("file"));
                return;
            }

            if (cmd.hasOption("md5Lite")) {
                String array[] = cmd.getOptionValue("md5Lite").split(",");
                client.getDoc(array);
                return;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
