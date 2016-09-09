package com.hcse.app.d2;

import java.util.List;
import java.util.Map;

import com.hcse.protocol.BaseRequest;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.SearchStringParser;

public class FileRequestLoader extends RequestLoader<BaseRequest> {
    private String filename;

    public FileRequestLoader(String filename) {
        this.filename = filename;
    }

    @Override
    public List<? extends BaseRequest> loadRequest(Map<String, String> shortNameMap, RequestFactory<BaseRequest> factory) {
        return (List<? extends BaseRequest>) SearchStringParser.loadRequest(filename, shortNameMap, factory);
    }
}
