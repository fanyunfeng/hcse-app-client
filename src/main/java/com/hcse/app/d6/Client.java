package com.hcse.app.d6;

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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import com.hcse.app.ExitException;
import com.hcse.protocol.d6.message.D6ResponseMessage;
import com.hcse.protocol.util.packet.BaseDoc;
import com.hcse.protocol.util.packet.BasePacket;

enum DocContentFormat {
    array, object
};

public class Client extends ClientBase {
    protected final Logger logger = Logger.getLogger(Client.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected DocContentFormat fieldFormat = DocContentFormat.array;
    protected String dir = ".";

    protected boolean save = false;
    protected boolean pretty = true;
    protected int firstLevelLength = 3;

    protected String charset = "utf8";

    static class MyPrettyPrinter extends DefaultPrettyPrinter {
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
            logger.error("dump response failed.", e);
        } catch (IOException e) {
            logger.error("dump response failed.", e);
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

    private void getOneDoc(String midAndDoc) {
        String[] fields = midAndDoc.split(":");

        try {
            if (fields.length == 2) {
                getOneDoc(Integer.parseInt(fields[0]), fields[1]);
            } else {
                getOneDoc(defalutMid, fields[0]);
            }
        } catch (NumberFormatException e) {
            logger.error("parse machine id failed.", e);
        }
    }

    private void getOneDoc(int mid, String md5Lite) {
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
            logger.error("get doc from server failed.", e);
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

            logger.error("get doc from server failed.", e);
        }
    }

    private void getDoc(String[] md5LiteList) {
        for (String v : md5LiteList) {
            getOneDoc(v);
        }
    }

    @SuppressWarnings("static-access")
    protected void init() throws ExitException {
        super.init();

        options.addOption(OptionBuilder.withLongOpt("file").withDescription("md5 file name.").hasArg()
                .withArgName("file").create('f'));

        options.addOption(OptionBuilder.withLongOpt("md5Lite").withDescription("md5Lite list split by ','").hasArg()
                .withArgName("md5Lite").create('m'));

        options.addOption(OptionBuilder.withLongOpt("directory").withDescription("directory to save result").hasArg()
                .withArgName("directory").create('d'));

        options.addOption(OptionBuilder.withLongOpt("pretty").withDescription("print pretty format. true/false")
                .hasArg().withArgName("pretty").create());

        options.addOption(OptionBuilder.withLongOpt("array").withDescription("print document field by json array.")
                .withArgName("array").create());

        options.addOption(OptionBuilder.withLongOpt("object").withDescription("print document field by json array.")
                .withArgName("object").create());

        options.addOption(OptionBuilder.withLongOpt("charset").withDescription("charset to encoding JSON.")
                .withArgName("charset").create());
    }

    protected void parseArgs(CommandLine cmd) throws ExitException {
        super.parseArgs(cmd);

        if (cmd.hasOption("directory")) {
            save = true;
            dir = cmd.getOptionValue("directory");
        }

        if (cmd.hasOption("pretty")) {
            String value = cmd.getOptionValue("pretty");

            value = value.toLowerCase();

            if (value.equals("yes") || value.equalsIgnoreCase("true") || value.equals("1")) {
                pretty = true;
            }
        }

        if (cmd.hasOption("charset")) {
            charset = cmd.getOptionValue("charset");
        }

        if (cmd.hasOption("array")) {
            fieldFormat = DocContentFormat.array;
        }

        if (cmd.hasOption("object")) {
            fieldFormat = DocContentFormat.object;
        }
    }

    protected void run(CommandLine cmd) {
        if (cmd.hasOption("file")) {
            getDoc(cmd.getOptionValue("file"));
            return;
        }

        if (cmd.hasOption("md5Lite")) {
            String array[] = cmd.getOptionValue("md5Lite").split(",");
            getDoc(array);
            return;
        }
    }

    public static void main(String[] args) {
        ClientBase client = new Client();

        client.entry(args);
    }
}
