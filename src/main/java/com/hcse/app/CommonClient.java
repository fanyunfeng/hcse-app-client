package com.hcse.app;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.mina.filter.codec.ProtocolCodecFactory;

import com.hcse.protocol.BaseRequest;
import com.hcse.protocol.BaseResponse;
import com.hcse.protocol.BaseResponseDoc;
import com.hcse.protocol.dump.OutputStreamBuilder;
import com.hcse.protocol.dump.ResponseDump;
import com.hcse.protocol.handler.DocHandler;
import com.hcse.service.ConnectTimeout;
import com.hcse.service.RequestTimeout;
import com.hcse.service.SearchService;
import com.hcse.service.ServiceException;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.ShortNameMap;

public class CommonClient<RequestMessage extends BaseRequest, ResponseMessage extends BaseResponse, ResponseCodecFactory extends ProtocolCodecFactory>
        extends BaseClient {

    protected final Logger logger = Logger.getLogger(CommonClient.class);

    protected AtomicBoolean running = new AtomicBoolean(true);
    protected SearchService<RequestMessage, ResponseMessage, ResponseCodecFactory> service;

    protected List<RequestMessage> requests = new ArrayList<RequestMessage>();

    protected ResponseDump dumper = new ResponseDump();

    protected RequestFactory<RequestMessage> requestFactory;

    protected ResponseCodecFactory clientCodecFactory;

    protected RequestQueue<RequestMessage> requestQueue;

    protected RequestLoader requestLoader;

    protected OutputStreamBuilder outputStreamBuilder;

    protected ArrayList<DocHandler> handlers = new ArrayList<DocHandler>();

    protected Map<String, String> shortNameMap = ShortNameMap.getInstance();

    public void addDocHandler(DocHandler handler) {
        handlers.add(handler);
    }

    public SearchService<RequestMessage, ResponseMessage, ResponseCodecFactory> getService() {
        return service;
    }

    public void setService(SearchService<RequestMessage, ResponseMessage, ResponseCodecFactory> service) {
        this.service = service;
    }

    public void setDumper(ResponseDump dumper) {
        this.dumper = dumper;
    }

    public RequestFactory<RequestMessage> getRequestFactory() {
        return requestFactory;
    }

    public void setRequestFactory(RequestFactory<RequestMessage> requestFactory) {
        this.requestFactory = requestFactory;
    }

    public ResponseCodecFactory getClientCodecFactory() {
        return clientCodecFactory;
    }

    public void setClientCodecFactory(ResponseCodecFactory clientCodecFactory) {
        this.clientCodecFactory = clientCodecFactory;
    }

    public RequestQueue<RequestMessage> getRequestQueue() {
        return requestQueue;
    }

    public void setRequestQueue(RequestQueue<RequestMessage> requestQueue) {
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

    public CommonClient() {
        handler = new ClientEventsNullHandler();
    }

    public void init() {
        service.open(clientCodecFactory);

        requests = requestLoader.loadRequest(shortNameMap, requestFactory);

        requestQueue.setRequestList(requests);
    }

    public void stop() {
        running.set(false);

        service.close();
    }

    public void handleRequest(RequestMessage request) {
        ResponseMessage response = null;

        try {
            response = service.search(request, null);

            if (response != null) {
                handler.onLanch();
                List<BaseResponseDoc> list = (List<BaseResponseDoc>) response.getDocs();

                handler.onCompleted(list == null || list.isEmpty());

                if (list != null) {
                    for (BaseResponseDoc doc : list) {
                        for (DocHandler handler : handlers) {
                            handler.process(doc);
                        }
                    }

                    Collections.sort(list, new Comparator<BaseResponseDoc>() {
                        @Override
                        public int compare(BaseResponseDoc o1, BaseResponseDoc o2) {
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
            RequestMessage request = requestQueue.getRequest();

            if (request == null) {
                break;
            }

            handleRequest(request);
        }
    }
}
