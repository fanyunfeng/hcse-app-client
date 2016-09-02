package com.hcse.app.d2;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hcse.app.BaseClientContext;
import com.hcse.protocol.d2.codec.D2ClientCodecFactory;
import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.protocol.d2.message.D2ResponseMessage;
import com.hcse.protocol.d2.message.D2ResponseMessageDoc;
import com.hcse.protocol.dump.OutputStreamBuilder;
import com.hcse.protocol.dump.ResponseDump;
import com.hcse.protocol.handler.DocHandler;
import com.hcse.service.ServiceException;
import com.hcse.service.common.ServiceDiscoveryService;
import com.hcse.service.d2.IndexService;
import com.hcse.service.d2.IndexServiceImpl;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.ShortNameMap;

public class D2Client extends BaseClientContext {
    protected final Logger logger = Logger.getLogger(D2Client.class);

    private boolean running = false;
    private IndexService service;

    private List<D2RequestMessage> requests = new ArrayList<D2RequestMessage>();

    private ResponseDump dumper = new ResponseDump();

    private RequestFactory requestFactory;

    private D2ClientCodecFactory clientCodecFactory;

    private RequestQueue requestQueue;

    private RequestLoader requestLoader;

    private OutputStreamBuilder outputStreamBuilder;

    private ArrayList<DocHandler> handlers = new ArrayList<DocHandler>();

    private Map<String, String> shortNameMap = ShortNameMap.getInstance();

    public void addDocHandler(DocHandler handler) {
        handlers.add(handler);
    }

    void setDumper(ResponseDump dumper) {
        this.dumper = dumper;
    }

    public RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public D2ClientCodecFactory getClientCodecFactory() {
        return clientCodecFactory;
    }

    public void setClientCodecFactory(D2ClientCodecFactory clientCodecFactory) {
        this.clientCodecFactory = clientCodecFactory;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public RequestLoader getRequestLoader() {
        return requestLoader;
    }

    public void setRequestLoader(RequestLoader requestLoader) {
        this.requestLoader = requestLoader;
    }

    public OutputStreamBuilder getOutputStreamBuilder() {
        return outputStreamBuilder;
    }

    public void setOutputStreamBuilder(OutputStreamBuilder outputStreamBuilder) {
        this.outputStreamBuilder = outputStreamBuilder;
    }

    public Map<String, String> getShortNameMap() {
        return shortNameMap;
    }

    public D2Client() {

    }

    public void init() {
        IndexServiceImpl srv = new IndexServiceImpl();

        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        srv.setServiceDiscoveryService(serviceDiscovery);

        service = srv;

        service.open(clientCodecFactory);

        requests = requestLoader.LoadRequest(shortNameMap, requestFactory);

        requestQueue.setRequestList(requests);
    }

    public void run() throws MalformedURLException, ServiceException {

        while (running) {
            D2RequestMessage request = requestQueue.getRequest();

            if (request == null) {
                if (running) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {

                    }
                }
            }

            D2ResponseMessage response = service.search(request, null);

            if (response != null) {

                List<D2ResponseMessageDoc> list = response.getDocs();

                for (D2ResponseMessageDoc doc : list) {
                    for (DocHandler handler : handlers) {
                        handler.process(doc);
                    }
                }
                OutputStream os = outputStreamBuilder.creatOutputStream(request.getTag());

                dumper.dump(os, response);

                outputStreamBuilder.destory(os);
            }
        }
    }
}
