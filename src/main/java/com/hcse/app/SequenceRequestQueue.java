package com.hcse.app;

import java.util.Iterator;
import java.util.List;

import com.hcse.protocol.d2.message.D2RequestMessage;

public class SequenceRequestQueue<Request extends D2RequestMessage> extends RequestQueue<Request> {
    Iterator<Request> iterator;
    List<Request> list;

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
