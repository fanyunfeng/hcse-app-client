package com.hcse.app;

import com.hcse.protocol.BaseRequest;

public class RoundRequestQueue<Request extends BaseRequest> extends SequenceRequestQueue<Request> {
    public Request getRequest() {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            iterator = list.iterator();
            return iterator.next();
        }
    }
}
