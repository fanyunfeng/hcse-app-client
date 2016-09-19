package com.hcse.app;

import java.util.ArrayList;

import com.hcse.protocol.handler.DocHandler;
import com.hcse.protocol.handler.ResponseHandler;

public class BaseClient {
    protected ArrayList<DocHandler> docHandlers = new ArrayList<DocHandler>();
    protected ArrayList<ResponseHandler> responseHandler = new ArrayList<ResponseHandler>();

    public void run(BaseClientConf conf) {

    }

    public void init() {

    }

    public void stop() {

    }

    public void addDocHandler(DocHandler handler) {
        docHandlers.add(handler);
    }

    public void addResponseHandler(ResponseHandler handler) {
        responseHandler.add(handler);
    }
}
