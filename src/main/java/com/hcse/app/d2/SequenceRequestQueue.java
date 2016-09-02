package com.hcse.app.d2;

import java.util.Iterator;
import java.util.List;

import com.hcse.protocol.d2.message.D2RequestMessage;

public class SequenceRequestQueue extends RequestQueue {
    Iterator<D2RequestMessage> iterator;
    List<D2RequestMessage> list;

    public void setRequestList(List<D2RequestMessage> list) {
        this.list = list;
        iterator = list.iterator();
    }

    public D2RequestMessage getRequest() {
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }
}
