package com.hcse.app.d2;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.hcse.app.BaseClientConf;
import com.hcse.app.BaseClientContext;
import com.hcse.protocol.d2.codec.D2ClientCodecFactory;
import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.protocol.d2.message.D2ResponseMessage;
import com.hcse.protocol.d2.message.D2ResponseMessageDoc;
import com.hcse.protocol.dump.OutputStreamBuilder;
import com.hcse.protocol.dump.ResponseDump;
import com.hcse.protocol.handler.DocHandler;
import com.hcse.service.ConnectTimeout;
import com.hcse.service.RequestTimeout;
import com.hcse.service.ServiceException;
import com.hcse.service.common.ServiceDiscoveryService;
import com.hcse.service.d2.IndexService;
import com.hcse.service.d2.IndexServiceImpl;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.ShortNameMap;

public class D2Client extends BaseClientContext {
    protected final Logger logger = Logger.getLogger(D2Client.class);

    protected AtomicBoolean running = new AtomicBoolean(true);
    private IndexService service;

    private List<D2RequestMessage> requests = new ArrayList<D2RequestMessage>();

    private ResponseDump dumper = new ResponseDump();

    private RequestFactory<D2RequestMessage> requestFactory;

    private D2ClientCodecFactory clientCodecFactory;

    protected RequestQueue<D2RequestMessage> requestQueue;

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

    public RequestFactory<D2RequestMessage> getRequestFactory() {
        return requestFactory;
    }

    public void setRequestFactory(RequestFactory<D2RequestMessage> requestFactory) {
        this.requestFactory = requestFactory;
    }

    public D2ClientCodecFactory getClientCodecFactory() {
        return clientCodecFactory;
    }

    public void setClientCodecFactory(D2ClientCodecFactory clientCodecFactory) {
        this.clientCodecFactory = clientCodecFactory;
    }

    public RequestQueue<D2RequestMessage> getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(RequestQueue<D2RequestMessage> requestQueue) {
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

    protected ClientEvents handler;

    public D2Client() {
        handler = new ClientEventsNullHandler();
    }

    public void init() {
        IndexServiceImpl srv = new IndexServiceImpl();

        ServiceDiscoveryService serviceDiscovery = new ServiceDiscoveryService();

        srv.setServiceDiscoveryService(serviceDiscovery);

        service = srv;

        service.open(clientCodecFactory);

        requests = requestLoader.loadRequest(shortNameMap, requestFactory);

        requestQueue.setRequestList(requests);
    }

    public void stop() {
        running.set(false);

        service.close();
    }

    public void handleRequest(D2RequestMessage request) {
        D2ResponseMessage response = null;

        try {
            response = service.search(request, null);

            if (response != null) {
                handler.onLanch();
                List<D2ResponseMessageDoc> list = response.getDocs();

                handler.onCompleted(list == null || list.isEmpty());

                if (list != null) {
                    for (D2ResponseMessageDoc doc : list) {
                        for (DocHandler handler : handlers) {
                            handler.process(doc);
                        }
                    }

                    Collections.sort(list, new Comparator<D2ResponseMessageDoc>() {
                        @Override
                        public int compare(D2ResponseMessageDoc o1, D2ResponseMessageDoc o2) {
                            if (o1.getWeight() == o2.getWeight()) {
                                if (o1.getMd5Lite() < o2.getMd5Lite()) {
                                    return -1;
                                } else if (o1.getMd5Lite() == o2.getMd5Lite()) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            } else if (o1.getWeight() < o2.getWeight()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }

                    });

                    OutputStream os = outputStreamBuilder.creatOutputStream(request.getTag());

                    if (os != null)
                        dumper.dump(os, response);

                    outputStreamBuilder.destory(os);
                }

            }
        } catch (ConnectTimeout e) {
            handler.onConnectTimeout();
        } catch (RequestTimeout e) {
            handler.onConnectTimeout();
        } catch (MalformedURLException | ServiceException e) {
            handler.onFailed();
            e.printStackTrace();
        }
    }

    public void run(BaseClientConf conf) {
        while (running.get()) {
            D2RequestMessage request = requestQueue.getRequest();

            if (request == null) {
                break;
            }

            handleRequest(request);
        }
    }
}
