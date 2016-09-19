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
import com.hcse.protocol.handler.ResponseHandler;
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

    protected Map<String, String> shortNameMap = ShortNameMap.getInstance();

    protected ClientRunner runner;

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

    public ClientRunner getRunner() {
        return runner;
    }

    public void setClientRunner(ClientRunner runner) {
        this.runner = runner;
    }

    protected ClientEvents clientEventHandler;

    public CommonClient() {
        clientEventHandler = new ClientEventsNullHandler();
    }

    public void init() {
        logger.info("client initialize.");
        service.open(clientCodecFactory);

        logger.info("client loading request.");
        requests = requestLoader.loadRequest(shortNameMap, requestFactory);

        requestQueue.setRequestList(requests);
    }

    public void stop() {
        running.set(false);

        service.close();
    }

    public ClientEvents getClientEventHandler() {
        return clientEventHandler;
    }

    public void setClientEventHandler(ClientEvents clientEventHandler) {
        this.clientEventHandler = clientEventHandler;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void handleRequest(RunnerContext ctx, RequestMessage request) {
        ResponseMessage response = null;

        try {
            response = service.search(request, null);

            if (response != null) {
                clientEventHandler.onLanch(ctx);
                List<BaseResponseDoc> list = (List<BaseResponseDoc>) response.getDocs();

                clientEventHandler.onCompleted(ctx, list == null || list.isEmpty());

                if (list != null) {
                    for (DocHandler handler : docHandlers) {
                        handler.reset();
                    }

                    for (BaseResponseDoc doc : list) {
                        for (DocHandler handler : docHandlers) {
                            handler.process(doc);
                        }
                    }

                    for (ResponseHandler handler : responseHandler) {
                        handler.reset();
                        handler.process(response);
                    }

                    OutputStream os = outputStreamBuilder.creatOutputStream(request.getTag());

                    if (os != null)
                        dumper.dump(os, response);

                    outputStreamBuilder.destory(os);
                }

            }
        } catch (ConnectTimeout e) {
            clientEventHandler.onConnectTimeout(ctx);
        } catch (RequestTimeout e) {
            clientEventHandler.onConnectTimeout(ctx);
        } catch (MalformedURLException | ServiceException e) {
            clientEventHandler.onFailed(ctx);
            e.printStackTrace();
        }
    }

    public void run(BaseClientConf conf) {
        logger.info("client start.");
        runner.run(conf, this);
        logger.info("client stopped.");
    }
}
