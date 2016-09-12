package com.hcse.app;

import java.util.Iterator;
import java.util.List;

import com.hcse.protocol.BaseRequest;

public class SequenceRequestQueue<Request extends BaseRequest> extends RequestQueue<Request> {
    protected Iterator<Request> iterator;
    protected List<Request> list;

    public void setRequestList(List<Request> list) {
        this.list = list;
        iterator = list.iterator();
    }

    public Request getRequest() {
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }
}
