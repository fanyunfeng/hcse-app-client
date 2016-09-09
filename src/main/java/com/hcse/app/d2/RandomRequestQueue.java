package com.hcse.app.d2;

import java.util.List;
import java.util.Random;

import com.hcse.protocol.d2.message.D2RequestMessage;

public class RandomRequestQueue<Request extends D2RequestMessage> extends RequestQueue<Request> {
    private int size;
    private List<Request> list;
    private Random random = new Random(System.currentTimeMillis());

    public void setRequestList(List<Request> list) {
        this.list = list;
        this.size = list.size();
    }

    public Request getRequest() {
        int id = random.nextInt(size);

        return list.get(id);
    }
}
