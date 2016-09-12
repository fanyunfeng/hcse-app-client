package com.hcse.app;

import java.util.List;
import java.util.Random;

import com.hcse.protocol.BaseRequest;

public class RandomRequestQueue<Request extends BaseRequest> extends RequestQueue<Request> {
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
