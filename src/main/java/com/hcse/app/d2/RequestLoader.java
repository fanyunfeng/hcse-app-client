package com.hcse.app.d2;

import java.util.List;
import java.util.Map;

import com.hcse.service.BaseRequest;
import com.hcse.util.sstring.RequestFactory;

public class RequestLoader<Request extends BaseRequest> {

    public List<? extends Request> loadRequest(Map<String, String> shortNameMap, RequestFactory<Request> factory) {
        return null;
    }
}
